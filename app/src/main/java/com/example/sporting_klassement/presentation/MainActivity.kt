/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.sporting_klassement.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.sporting_klassement.R
import com.example.sporting_klassement.presentation.theme.Sporting_klassementTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


// je moet in AndroidManifest.xml de volgende permission toevoegen:
// <uses-permission android:name="android.permission.INTERNET" />

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scope = CoroutineScope(Dispatchers.Main)

        // Start the scraping process in a background thread
        scope.launch {
            val url = "https://www.voetbalexpress.be/seizoen2023-2024/herenamateurs1.html"
            val doc: Document = withContext(Dispatchers.IO) {
                Jsoup.connect(url).get()
            }
            val title: String = doc.title()
            Log.d("WebScraping", "Title: $title")

            val table: Element? = doc.select("table.content_table.tab_klassement").first()

            val tableData: List<List<String>> = if (table != null) {
                val tbody: Element? = table.select("tbody").first()
                if (tbody != null) {

                    // Extract the header cell texts if the header row exists
                    val headerRow: Element? = tbody?.select("tr")?.firstOrNull()
                    val headerCells: List<String> =
                        headerRow?.select("td.tab_klass_header_ploeg, td.tab_klass_header_A, td.tab_klass_header_P")
                            ?.map { cell ->
                                val text = cell.select("b").text()
                                if (text == "A") {
                                    "gesp. wed."
                                } else {
                                    text
                                }
                            } ?: emptyList()


                    // data zelf
                    val rows: Elements = tbody.select("tr")
                    val rowData: List<List<String>> = rows.map { row ->
                        val cells: Elements =
                            row.select("td.tab_klass_volgnr, td.tab_klass_ploeg, td.tab_klass_A, td.tab_klass_P")
                        cells.map { cell -> cell.text() }
                    }

                    listOf(headerCells) + rowData
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }


            // Update the UI with the scraped data
            setContent {
                WearApp(tableData)
            }
        }
    }
}

// @Composable zorgt ervoor dat toolkit enabled wordt zodat je declaratief kan schrijven
// via Jetpack Compose toolkit
@Composable
fun WearApp(tableData: List<List<String>>) {
    Sporting_klassementTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
//                .padding(3.dp)
        ) {
            Table(tableData)
        }
    }
}

@Composable
fun Table(tableData: List<List<String>>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // padding bovenaan zetten, anders kan je eerste rij niet volledig zien
        item {
            Spacer(modifier = Modifier.height(55.dp))
        }
        items(tableData.size) { rowIndex ->
            val row = tableData[rowIndex]
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { cellData ->
                    val cellModifier = if (rowIndex == 0) {
                        Modifier.padding(start = 12.dp, end = 1.dp)
                    } else {
                        Modifier.padding(6.dp)
                    }
                    Cell(cellData = cellData, modifier = cellModifier)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(55.dp))
        }

    }
}


@Composable
fun Cell(cellData: String, modifier: Modifier) {
    Text(
        modifier = modifier,
        text = cellData
    )
}


@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}


@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
//    val title = "Preview Android"
    val tableData = listOf(
        listOf("Row 1 Cell 1", "Row 1 Cell 2"),
        listOf("Row 2 Cell 1", "Row 2 Cell 2")
    )
    WearApp(tableData)
}