import csstype.ClassName
import dom.html.HTMLInputElement
import dom.html.HTMLTextAreaElement
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.textarea
import react.dom.html.ReactHTML.ul
import react.useState

const val LS_TODOS_KEY = "_kjs-todo_"
const val LS_EXISTING_USER_FLAG = "_kjs-todo-sys_existing-user"
const val LS_EXISTING_USER_VALUE = "true"

@Serializable
data class TodoItem(
    val id: Int,
    val title: String,
    val content: String,

    val name: String = LS_TODOS_KEY + id.toString()
)

@Serializable
data class TodoItemWithState(
    val todoItem: TodoItem,
    val completed: Boolean
)

val defaultTodos = listOf(
    TodoItemWithState(
        TodoItem(
            2, "Welcome to the todo list!", """
            This is a simple todo list made with Kotlin/JS.

            Add a new todo item by filling out the form on the right.
            You can also delete or mark items as completed by clicking the buttons below.
        """.trimIndent()
        ), true
    ),
    TodoItemWithState(
        TodoItem(
            1, "Shopping list", """
            - Apples
            - Hearty Durian
            - Mighty Bananas
            - Spicy Pepper
        """.trimIndent()
        ), false
    ),
    TodoItemWithState(
        TodoItem(
            0, "TODO list appreciation", """
            - Create a new todo item
            - Mark it as completed
            - Reload the page
            - Sort by completion state
            - Delete an item
            - Reload the page
        """.trimIndent()
        ), false
    )
).reversed()

fun getTodos(): Pair<List<TodoItem>, Map<Int, Boolean>> {
    val todos = mutableListOf<TodoItem>()
    val completionState = mutableMapOf<Int, Boolean>()

    when (localStorage.getItem(LS_EXISTING_USER_FLAG)) {
        LS_EXISTING_USER_VALUE -> {
            @Suppress("USE_LAST_INDEX")
            (localStorage.length - 1 downTo 0)
                .mapNotNull { index ->
                    localStorage.key(index)
                        ?.takeIf { it.startsWith(LS_TODOS_KEY) }
                        ?.let { localStorage.getItem(it) }
                }
                .map { Json.decodeFromString<TodoItemWithState>(it) }
                .sortedBy { it.todoItem.id }
        }

        else -> {
            localStorage.setItem(LS_EXISTING_USER_FLAG, LS_EXISTING_USER_VALUE)

            defaultTodos.forEach { item ->
                localStorage.setItem(item.todoItem.name, Json.encodeToString(item))
            }
            defaultTodos
        }
    }.forEach {
        todos += it.todoItem
        completionState[it.todoItem.id] = it.completed
    }

    return todos to completionState
}

val App = FC<Props> {
    val (preTodos, preCompletionState) = getTodos()

    var todos: List<TodoItem> by useState(preTodos)
    var completionState: Map<Int, Boolean> by useState(preCompletionState)
    var completedShown: Boolean by useState(true)
    var incompleteShown: Boolean by useState(true)

    h1 {
        +"Todo list in Kotlin/JS"
    }

    div {
        id = "todo_list"

        input {
            id = "view_completed"
            type = InputType.checkbox
            checked = completedShown
            readOnly = true
            onClick = {
                if (completedShown && !incompleteShown) {
                    incompleteShown = true
                }
                completedShown = !completedShown
            }
        }
        label {
            htmlFor = "view_completed"
            +"Show completed"
        }

        input {
            id = "view_incomplete"
            type = InputType.checkbox
            checked = incompleteShown
            readOnly = true
            onClick = {
                if (!completedShown && incompleteShown) {
                    completedShown = true
                }
                incompleteShown = !incompleteShown
            }
        }
        label {
            htmlFor = "view_incomplete"
            +"Show incomplete"
        }

        ul {
            todos.filter {
                if (completionState[it.id] == true) completedShown else incompleteShown
            }.reversed().forEach { item ->
                li {
                    className = ClassName(if (completionState[item.id] == true) "completed" else "incomplete")

                    // <div> is needed here so that it can morph around "float: right" indicator
                    div {
                        div {
                            className = ClassName("indicator")
                        }
                        className = ClassName("title")
                        +item.title
                    }

                    p {
                        className = ClassName("content")
                        item.content.split("\n").forEachIndexed { index, line ->
                            if (index != 0) {
                                br()
                            }
                            +line
                        }
                    }

                    button {
                        className = ClassName("delete")
                        onClick = {
                            completionState = completionState.filterKeys { it != item.id }
                            todos = todos.filter { it.id != item.id }
                            localStorage.removeItem(item.name)
                        }
                    }

                    button {
                        className = ClassName("toggle")
                        onClick = {
                            val newState = !(completionState[item.id] ?: false)
                            completionState += (item.id to newState)
                            localStorage.setItem(item.name, Json.encodeToString(TodoItemWithState(item, newState)))
                        }
                    }
                }
            }
        }
    }

    div {
        id = "add_todo"

        input {
            id = "new_title"
            type = InputType.text
            placeholder = "Title"
        }

        textarea {
            id = "new_content"
            placeholder = "Content"
        }

        input {
            type = InputType.submit
            value = "Add"
            onClick = {
                val newTodoItem = TodoItem(
                    (todos.maxOfOrNull { it.id } ?: 0) + 1,
                    (document.getElementById("new_title") as HTMLInputElement).value,
                    (document.getElementById("new_content") as HTMLTextAreaElement).value
                )
                todos += newTodoItem
                completionState += (newTodoItem.id to false)
                localStorage.setItem(newTodoItem.name, Json.encodeToString(TodoItemWithState(newTodoItem, false)))
            }
        }

        input {
            id = "clear_all"
            type = InputType.button
            value = "Clear all"
            onClick = {
                localStorage.clear()
                val (newTodos, newCompletionState) = getTodos()
                todos = newTodos
                completionState = newCompletionState
            }
        }
    }
}
