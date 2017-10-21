package net.bekwam.brokerme

import javafx.scene.Scene
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import tornadofx.*

/**
 * TreeView-based UI
 *
 * ServerList contains Servers.  Servers contain Queues and Topics.
 *
 * ServerList
 *   Server 1
 *      Queue 1
 *      ...
 *      Topic 1
 *      ...
*    ...
 *
 * @author carl
 */

// model classes
class BRMServerList(var fileName : String, var servers : List<BRMServer> = emptyList())

class BRMServer(
        var name : String,
        var url : String,
        var queues : List<BRMQueue> = emptyList(),
        var topics : List<BRMTopic> = emptyList()
)

abstract class BRMEndpoint(var name : String)

class BRMQueue(name : String) : BRMEndpoint(name)

class BRMTopic(name : String) : BRMEndpoint(name)

// ui code

class BrokerMeApp : App(MainView::class) {
    override fun createPrimaryScene(view: UIComponent) =
            Scene( view.root, 1024.0, 768.0 )
}

class MainView : View("Broker Me") {

    // test data
    private val localhostQueues = listOf(
            BRMQueue("queue1"),
            BRMQueue("queue2"),
            BRMQueue("queue3")
    )

    private val bekwamcomQueues = listOf(
            BRMQueue("Inbound"),
            BRMQueue("Outbound"),
            BRMQueue("ExpiryQueue"),
            BRMQueue("DLQ")
    )

    private val bekwamnetQueues = listOf(
            BRMQueue("queueA"),
            BRMQueue("queueB")
    )

    private val localhostTopics = listOf(
            BRMTopic("topic1"),
            BRMTopic("SystemAlerts"),
            BRMTopic("notifications")
    )

    private val bekwamcomTopics = listOf(
            BRMTopic("MyTopic")
    )

    private val bekwamnetTopics = listOf(
            BRMTopic("topicA")
    )

    private val servers = listOf(
            BRMServer("localhost", "tcp://localhost:61616", localhostQueues, localhostTopics),
            BRMServer("www.bekwam.com", "tcp://www.bekwam.com:61616", bekwamcomQueues, bekwamcomTopics),
            BRMServer("www.bekwam.net", "tcp://www.bekwam.net:61616", bekwamnetQueues, bekwamnetTopics)
    )

    private val serverList = BRMServerList("myservers.json", servers)

    override val root = vbox {

        treeview<Triple<String, String, Any?>> {

            root = TreeItem(Triple("root", serverList.fileName, null))
            root.isExpanded = true

            cellFormat {

                if( contextMenu != null ) contextMenu.items.clear()

                if( it == root.value ) {
                    text = "Servers - ${it.second}"
                    contextmenu {
                        item("Add Server")
                    }
                } else if( it.third!! is BRMServer ) {
                    text = "${it.first} (${it.second})"
                    contextmenu {
                        item("Rename")
                        item("Delete")
                    }
                } else if( it.third!! is BRMQueue ) {
                    text = "${it.first} QUEUE"
                    contextmenu {
                        item("Browse")
                        item("Rename")
                        item("Delete")
                    }
                } else if( it.third!! is BRMTopic ) {
                    text = "${it.first} TOPIC"
                    contextmenu {
                        item("Subscribe")
                        item("Rename")
                        item("Delete")
                    }
                } else {

                    text = it.first

                    if( it.first == "Queues" ) {
                        contextmenu {
                            item("Add Queue")
                        }
                    } else if( it.first == "Topics" ) {
                        contextmenu {
                            item("Add Topic")
                        }
                    }
                }

            }

            populate {

                parent ->

                    if( parent == root ) {

                        serverList
                                .servers
                                .map { Triple(it.name, it.url, it) }

                    } else {

                        when( parent.value.third!! ) {

                            is BRMServer ->

                                listOf(
                                    Triple("Queues", "", (parent.value.third!! as BRMServer).queues),
                                    Triple("Topics", "", (parent.value.third!! as BRMServer).topics)
                                )

                            is List<*> ->

                                @Suppress("UNCHECKED_CAST")
                                (parent.value.third!! as List<BRMEndpoint>).map { Triple(it.name, "", it) }

                            else ->

                                null
                        }
                    }
            }

            vgrow = Priority.ALWAYS
        }
    }
}