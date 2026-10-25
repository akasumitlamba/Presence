package app.presence.desktop

import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.Struct
import org.freedesktop.dbus.Tuple
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.annotations.Position
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt32
import org.freedesktop.dbus.types.Variant
import java.awt.EventQueue
import javax.imageio.ImageIO

@DBusInterfaceName("org.kde.StatusNotifierWatcher")
interface TrayWatcher : DBusInterface { fun RegisterStatusNotifierItem(service: String) }
@DBusInterfaceName("org.kde.StatusNotifierItem")
interface TrayItem : DBusInterface {
    fun Activate(x: Int, y: Int)
    fun SecondaryActivate(x: Int, y: Int)
    fun ContextMenu(x: Int, y: Int)
    fun Scroll(delta: Int, orientation: String)
}
@DBusInterfaceName("com.canonical.dbusmenu")
interface TrayMenu : DBusInterface {
    fun GetLayout(parentId: Int, recursionDepth: Int, propertyNames: List<String>): MenuLayoutReply<UInt32,MenuLayout>
    fun GetGroupProperties(ids: List<Int>, propertyNames: List<String>): List<MenuProperties>
    fun GetProperty(id: Int, name: String): Variant<*>
    fun Event(id: Int, eventId: String, data: Variant<*>, timestamp: UInt32)
    fun AboutToShow(id: Int): Boolean
}
@DBusInterfaceName("org.freedesktop.Notifications")
interface LinuxNotifications : DBusInterface {
    fun Notify(app: String, replaces: UInt32, icon: String, summary: String, body: String, actions: List<String>, hints: Map<String, Variant<*>>, timeout: Int): UInt32
}
class TrayPixmap(@field:Position(0) @JvmField val width: Int, @field:Position(1) @JvmField val height: Int, @field:Position(2) @JvmField val pixels: ByteArray) : Struct()
class TrayTooltip(@field:Position(0) @JvmField val name: String, @field:Position(1) @JvmField val images: List<TrayPixmap>, @field:Position(2) @JvmField val title: String, @field:Position(3) @JvmField val text: String) : Struct()
class MenuLayout(@field:Position(0) @JvmField val id: Int, @field:Position(1) @JvmField val properties: Map<String, Variant<*>>, @field:Position(2) @JvmField val children: List<Variant<*>>) : Struct()
class MenuLayoutReply<A,B>(@field:Position(0) @JvmField val revision: A, @field:Position(1) @JvmField val layout: B) : Tuple()
class MenuProperties(@field:Position(0) @JvmField val id: Int, @field:Position(1) @JvmField val properties: Map<String, Variant<*>>) : Struct()

/** Native ARGB StatusNotifier icons avoid AWT/XEmbed's opaque white Linux background. */
class LinuxTray(private val open: () -> Unit, private val toggle: () -> Unit, private val stop: () -> Unit, private val widget: (String) -> Unit, private val quit: () -> Unit) : AutoCloseable {
    private val connection = DBusConnectionBuilder.forSessionBus().withShared(false).build()
    private val service = "app.presence.Presence.StatusNotifier.p${ProcessHandle.current().pid()}"
    private val pixmaps = run {
        val image = ImageIO.read(javaClass.getResourceAsStream("/presence.png"))
        val bytes = ByteArray(image.width * image.height * 4)
        var i = 0
        for (y in 0 until image.height) for (x in 0 until image.width) {
            val pixel = image.getRGB(x,y)
            bytes[i++] = (pixel ushr 24).toByte(); bytes[i++] = (pixel ushr 16).toByte()
            bytes[i++] = (pixel ushr 8).toByte(); bytes[i++] = pixel.toByte()
        }
        listOf(TrayPixmap(image.width,image.height,bytes))
    }
    private fun dispatch(action: () -> Unit) { EventQueue.invokeLater(action) }
    private val item = object : TrayItem, Properties {
        override fun getObjectPath() = "/StatusNotifierItem"
        override fun Activate(x: Int,y: Int) = dispatch(open)
        override fun SecondaryActivate(x: Int,y: Int) = dispatch(toggle)
        override fun ContextMenu(x: Int,y: Int) = dispatch(open)
        override fun Scroll(delta: Int, orientation: String) {}
        override fun GetAll(interfaceName: String): Map<String,Variant<*>> = mapOf(
            "Category" to Variant("ApplicationStatus"), "Id" to Variant("Presence"), "Title" to Variant("Presence"),
            "Status" to Variant("Active"), "WindowId" to Variant(UInt32(0)), "IconName" to Variant(""),
            "IconPixmap" to Variant(pixmaps,"a(iiay)"), "OverlayIconName" to Variant(""),
            "OverlayIconPixmap" to Variant(emptyList<TrayPixmap>(),"a(iiay)"), "AttentionIconName" to Variant(""),
            "AttentionIconPixmap" to Variant(emptyList<TrayPixmap>(),"a(iiay)"), "AttentionMovieName" to Variant(""),
            "ToolTip" to Variant(TrayTooltip("",pixmaps,"Presence","Focus timer and time tracker"),"(sa(iiay)ss)"),
            "ItemIsMenu" to Variant(false), "Menu" to Variant(DBusPath("/Menu"))
        )
        @Suppress("UNCHECKED_CAST") override fun <A> Get(interfaceName: String,propertyName: String): A = GetAll(interfaceName).getValue(propertyName) as A
        override fun <A> Set(interfaceName: String,propertyName: String,value: A) { throw UnsupportedOperationException("Read-only tray properties") }
    }
    private val labels = mapOf(1 to "Open Presence",2 to "Continue / Pause",3 to "Stop",4 to "Timer widget",5 to "Statistics widget",6 to "Streak widget",7 to "Todos widget",8 to "Quit Presence")
    private fun properties(id: Int): Map<String,Variant<*>> = if (id == 0) mapOf("children-display" to Variant("submenu")) else mapOf("label" to Variant(labels[id].orEmpty()),"enabled" to Variant(true),"visible" to Variant(true))
    private val menu = object : TrayMenu, Properties {
        override fun getObjectPath() = "/Menu"
        override fun GetLayout(parentId: Int, recursionDepth: Int, propertyNames: List<String>) = MenuLayoutReply(UInt32(1),MenuLayout(parentId,properties(parentId),if (parentId == 0) labels.keys.map { Variant(MenuLayout(it,properties(it),emptyList()),"(ia{sv}av)") } else emptyList()))
        override fun GetGroupProperties(ids: List<Int>, propertyNames: List<String>) = ids.map { MenuProperties(it,properties(it)) }
        override fun GetProperty(id: Int,name: String) = properties(id).getValue(name)
        override fun Event(id: Int,eventId: String,data: Variant<*>,timestamp: UInt32) {
            if(eventId == "clicked") dispatch { when(id) { 1 -> open(); 2 -> toggle(); 3 -> stop(); 4 -> widget("timer"); 5 -> widget("stats"); 6 -> widget("streak"); 7 -> widget("todos"); 8 -> quit() } }
        }
        override fun AboutToShow(id: Int) = false
        override fun GetAll(interfaceName: String): Map<String,Variant<*>> = mapOf("Version" to Variant(UInt32(3)),"TextDirection" to Variant("ltr"),"Status" to Variant("normal"),"IconThemePath" to Variant(emptyList<String>(),"as"))
        @Suppress("UNCHECKED_CAST") override fun <A> Get(interfaceName: String,propertyName: String): A = GetAll(interfaceName).getValue(propertyName) as A
        override fun <A> Set(interfaceName: String,propertyName: String,value: A) { throw UnsupportedOperationException("Read-only menu properties") }
    }
    init {
        try {
            connection.requestBusName(service)
            connection.exportObject(item)
            connection.exportObject(menu)
            connection.getRemoteObject("org.kde.StatusNotifierWatcher","/StatusNotifierWatcher",TrayWatcher::class.java).RegisterStatusNotifierItem(service)
        } catch (error: Exception) { connection.close(); throw error }
    }
    fun notify(title: String, body: String) { runCatching {
        connection.getRemoteObject("org.freedesktop.Notifications","/org/freedesktop/Notifications",LinuxNotifications::class.java)
            .Notify("Presence",UInt32(0),"",title,body,emptyList(),emptyMap(),7000)
    } }
    override fun close() { connection.close() }
}
