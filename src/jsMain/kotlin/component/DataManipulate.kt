package component

import react.*
import react.dom.html.ReactHTML.b
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3

external interface DataManipulateProps : Props {
    var deleteMutation: () -> Unit
}

val CDataManipulate = FC<DataManipulateProps>("DataManipulate") { props ->
    /* Работа с полезными данными после обработки */
    h3 { +"Манипуляция с данными:" }
    div {
        button {
            b { + "Удаление данных" }
            onClick = { props.deleteMutation() }
        }
    }
}