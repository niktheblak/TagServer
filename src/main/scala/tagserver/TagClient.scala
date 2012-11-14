package tagserver

import java.io.{File, PrintStream}
import scala.actors.Actor._
import scala.actors.Actor
import tagserver.tag.{Keys, Tag}
import tagserver.server.TagServer
import tagserver.server.commands._

class StatusListener(output: PrintStream) extends Actor {
  def act() {
    loop {
      react {
        case TagsReloaded => output.println("Tag database reloaded.")
        case BaseDirAdded(dir: String) =>
          output.println("Base directory " + dir + " added.")
        case BaseDirRemoved(dir: String) =>
          output.println("Base directory " + dir + " removed.")
        case Quit => exit()
      }
    }
  }
}

class TagClient(server: TagServer) extends Actor {
  private def printTag(t: Tag) {
    if (t.contains(Keys.artist) && t.contains(Keys.title)) {
      Console.println(t.artist + ": " + t.title)
    } else {
      Console.println(t.toString)
    }
  }

  def act() {
    val listener = new StatusListener(System.out)
    server ! AddListener(listener)
    listener.start()
    loop {
      val commandString = Console.readLine(">")
      val tokens = commandString.split(" ").toList
      val command = tokens.head
      val args = (if (tokens.length > 1) tokens.tail else Nil)

      if (args.isEmpty) {
        // Handle commands without arguments
        command match {
          case "ping" =>
            val startTime = System.currentTimeMillis
            server ! Ping
            react {
              case Pong =>
                val stopTime = System.currentTimeMillis
                Console.println("Pong in " + (stopTime - startTime) + " ms")
            }
          case "exit" =>
            Console.println("Exiting...")
            server ! RemoveListener(listener)
            listener ! Quit
            server ! Quit
            exit()
          case "reload" =>
            Console.println("Reloading tags...")
            server ! ReloadTagsRequest
          case "reloadasync" =>
            Console.println("Reloading tags...")
            server ! ReloadTagsAsyncRequest
            var listenForMore = true
            loopWhile(listenForMore) {
	            react {
	              case TagDiscovered(tag) =>
	                printTag(tag)
	              case ReloadTagsAsyncReply =>
	                listenForMore = false
	                Console.println("Tag reload complete.")
	            }
            }
          case "list" =>
            val reply = server !? GetTagsRequest
            reply match {
              case GetTagsReply(tags) =>
                if (!tags.isEmpty) {
                  tags foreach printTag
                } else {
                  Console.println("The database does not contain any tags")
                }
            }
          case "listdirs" =>
            val reply = server !? GetBaseDirectoriesRequest
            reply match {
              case GetBaseDirectoriesReply(dirs) =>
                dirs foreach Console.println
            }
          case _ => System.out.println("Invalid command.")
        }
      } else {
        // Handle commands with arguments

        val firstSpace = commandString.indexOf(' ')
        val argString = commandString.substring(firstSpace + 1)

        command match {
          case "adddir" =>
            val dir = new File(argString)
            if (dir.exists && dir.isDirectory) {
              server ! AddBaseDirectory(argString)
            } else {
              Console.println("Directory " + argString + " does not exist")
            }
          case "removedir" =>
            server ! RemoveBaseDirectory(argString)
          case "search" =>
            val reply = server !? SearchRequest(argString)
            reply match {
              case SearchReply(tags) =>
                tags foreach { printTag(_) }
            }
          case _ => Console.println("Invalid command")
        }
      }
    }
  }
}
