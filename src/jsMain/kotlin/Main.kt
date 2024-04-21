import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.body ?: error("Couldn't find `body` in HTML!")
    createRoot(container).render(App.create())
}
