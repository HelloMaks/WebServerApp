package component

import react.FC
import react.Props
import ru.altmanea.webapp.data.Teacher
import web.prompts.alert

external interface ParserProps : Props {
    var innerCSV: String
    var setInnerFile: (String?) -> Unit
    var loadData: (List<Teacher>) -> Unit
}

typealias Index = Int // Порядок заголовка в таблице
typealias ExcelCell = String // Excel-ячейка
typealias ExcelRow = String // Excel-строка

val CSVParser = FC<ParserProps> ("CSVParser") { props ->
    /* Разделение текста на Excel-строки */
    val rows: List<ExcelRow> = props.innerCSV
        .replace(Regex("(?<=;)([^\";]*)\n")) { it.value + "{SPR}" } // Вычисление концов Excel-строк
        .replace("\"", "") // Удаление кавычек
        .split("\n{SPR}") // Разделение единого текста на множество строк

    var error = false // Регистрация ошибки неудачного поиска заголовков
    var search = false // Регистрация прохождения этапа поиска заголовков
    var searchIdx = 0 // Индекс для выявления строки с заголовками

    /* Соотношение преподавателя, дисциплины и групп студентов */
    val triples: MutableList<Triple<String, String, List<String>>> = mutableListOf()
    val needIdx: Array<Index> = Array(3) { -1 } // Индексы заголовков

    /* Поиск нужных заголовков Excel-таблицы */
    while(!search && !error) {
        if(rows.size > searchIdx) {
            rows[searchIdx].let { row ->
                val result = Regex("Преподавател[ьи]|Дисциплин[аы]|Групп[аы]")
                    .containsMatchIn(row) // Поиск необходимых заголовков

                if (result) {
                    row.split(";").forEachIndexed { idx, cell ->
                        when(cell.substringBefore(" ").substringAfter(" ")) {
                            "Преподаватель", "Преподаватели" -> needIdx[0] = idx
                            "Дисциплина", "Дисциплины" -> needIdx[1] = idx
                            "Группа", "Группы" -> needIdx[2] = idx
                        }
                    }
                    search = true
                }
            }
            searchIdx++
        }
        else { error = true }
    }

    if(error) { alert("ОШИБКА! Не были найдены заголовки!"); props.setInnerFile(null) }

    if(needIdx.none { it == -1 }) {
        /* Cоздание коллекции Excel-строк в виде коллекций Excel-ячеек */
        val listCells: List<List<ExcelCell>> = rows.map { row ->
            row.split(";") }.filterIndexed { idx, _ -> idx >= searchIdx }

        /* Проверка ячеек на соответствие данных и их занесение в массив */
        listCells.forEach { cells ->
            var teacher = ""; var discipline = ""
            var groups = listOf<String>()

            needIdx.forEachIndexed { idx, needIndex ->
                if(cells.size > needIndex) {
                    when(idx) {
                        /* Проверка ячеек с преподавателями */
                        0 -> Regex("([А-ЯЁ][а-яё]+)\\s([А-ЯЁ][а-яё]+)\\s?([А-ЯЁ][а-яё]+)?")
                        /* Проверка ячеек с дисциплинами */
                        1 -> Regex("([А-ЯЁ][а-яё]+)[\\s-]?(([а-яё]+)[\\s-]?)*")
                        /* Проверка ячеек с группами */
                        else -> Regex("(\\d{2}(-[А-ЯЁ]{2}|[а-яё]),?\\s?)+")
                    }.find(cells[needIndex])?.value.let { data ->
                        if(idx == 0) teacher = data ?: "null"
                        if(idx == 1) discipline = data ?: "null"
                        else groups = data?.replace(" ", "")
                            ?.split(",") ?: emptyList()
                    }
                }
            }
            if((listOf(teacher, discipline) + groups).none { it == "null" || it == "" })
                triples.add(Triple(teacher, discipline, groups))
        }

        if(triples.isNotEmpty()) {
            props.setInnerFile(null)
            CDataBuild {
                this.triples = triples.distinct()
                this.loadData = { props.loadData(it) }
            }
        }
        else { alert("ОШИБКА! Не были найдены данные для импорта!"); props.setInnerFile(null) }
    } else if(!error) { alert("ОШИБКА! Не были найдены все нужные заголовки!"); props.setInnerFile(null) } }