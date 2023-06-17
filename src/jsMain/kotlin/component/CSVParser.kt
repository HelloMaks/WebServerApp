package component

import kotlinx.browser.window
import react.FC
import react.Props
import ru.altmanea.webapp.data.Teacher
import web.prompts.alert

external interface ParserProps : Props {
    var innerCSV: String
    var loadData: (List<Teacher>) -> Unit
}

typealias Header = String // Заголовок данных
typealias Index = Int // Порядок заголовка в таблице
typealias DataCell = String // Данные ячейки
typealias DataRow = String // Данные строки

val CSVParser = FC<ParserProps> ("CSVParser") { props ->
    /* Разделение текста на Excel-строки */
    val rows: List<DataRow> = props.innerCSV
        .replace(Regex("\"[,; ]"),"\"{SPR}") // Задание символа-разделителя для ячеек
        .split(Regex("\"\n")) // Разделение текста на строки
        .map { row -> row.replace("\"", "") } // Удаление кавычек

    /* Считывание таблицы, выявление необходимых данных */
    var teachHead: Header = "..." // Наименование заголовка с преподавателями
    var discipHead: Header = "..." // Наименование заголовка с дисциплинами
    var groupHead: Header = "..." // Наименование заголовка с группами

    var headIndex: Index = -1 // Порядок ячейки с заголовком в строке
    val needCells: MutableMap<Header, Index> = mutableMapOf() // Соотношение заголовков и их порядковых значений
    val dataList: MutableList<Map<Header, List<DataCell>>> = mutableListOf() // Коллекция измененных Excel-строк

    var error = false // Переменная, регистрирующая ошибки

    rows.forEach { row ->
        if(!error) {
            val cells: List<DataCell> = row.split("{SPR}") // Разделение строки на массив ячеек
            if(headIndex == -1) {
                val result = Regex("№|Номер")
                    .find(cells.joinToString(" ") { it }) // Поиск строки с заголовками

                result?.let {
                    headIndex = 0
                    cells.forEach { cell ->
                        val res = Regex("Преподавател[ьи]|Дисциплин[аы]|Групп[аы]")
                            .find(cell) // Поиск необходимых заголовков
                        res?.let { need ->
                            needCells[need.value] = headIndex
                            when (need.value) {
                                "Преподаватель", "Преподаватели" -> teachHead = need.value
                                "Дисциплина", "Дисциплины" -> discipHead = need.value
                                "Группа", "Группы" -> groupHead = need.value
                            }
                        }
                        headIndex++
                    }
                }
            }
            else if(needCells.size == 3) {
                if (needCells.filter { (it.value + 1) > cells.size }.isEmpty()) {
                    val filterCells: Map<Header, List<DataCell>> = needCells.mapValues { (header, index) ->
                        when (header) {
                            teachHead -> Regex("([А-ЯЁ][а-яё]+)")
                            discipHead -> Regex("([А-ЯЁ][а-яё])*([а-яё -]+)")
                            groupHead -> Regex("""\d+([А-ЯЁа-яё]+)|\d+-([А-ЯЁа-яё]+)""")
                            else -> Regex("EMPTY")
                        }.findAll(cells[index]).map { match -> match.value }.toList()
                    }
                    if (filterCells.none { it.value == emptyList<String>() } && filterCells !in dataList) {
                        dataList.add(filterCells)
                    }
                }
            }
            /* Регистрация ошибки, связанная с отсутствием нужных заголовков */
            else if(needCells.size != 3) {
                alert("ОШИБКА! При импорте данных не были найдены нужные заголовки...")
                error = true
            }
        }
        else { window.location.reload() } // Перезагрузка
    }

    if(dataList.isNotEmpty()) {
        CDataBuild {
            this.teachHead = teachHead
            this.discipHead = discipHead
            this.groupHead = groupHead
            this.dataList = dataList
            this.loadData = { props.loadData(it) }
        }
    }
}