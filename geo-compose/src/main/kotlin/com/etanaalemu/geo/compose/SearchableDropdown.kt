package com.etanaalemu.geo.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Searchable dropdown: tap the field to open a dialog with a search box and scrollable results.
 *
 * @param featuredSections Pinned groups shown at the top when the search box is empty (e.g. favorites).
 * @param onSearchQueryChange When set (e.g. for cities), each keystroke is forwarded for async/DB filtering.
 *   When null, [items] are filtered in-memory with [searchFilter].
 */
@Composable
fun <T> SearchableDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Select $label",
    emptyMessage: String = "No results found",
    itemKey: (T) -> Any = { it.hashCode() },
    searchFilter: (item: T, query: String) -> Boolean = { item, query ->
        if (query.isBlank()) true
        else itemLabel(item).contains(query, ignoreCase = true)
    },
    featuredSections: List<FeaturedSection<T>> = emptyList(),
    allSectionTitle: String = "All",
    onSearchQueryChange: ((String) -> Unit)? = null,
    externalSearchQuery: String = "",
    hasMoreItems: Boolean = false,
    isLoadingMore: Boolean = false,
    onLoadMore: (() -> Unit)? = null,
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogSearchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    val displayQuery = if (onSearchQueryChange != null) externalSearchQuery else dialogSearchQuery
    val isSearching = displayQuery.isNotBlank()

    val filteredMainItems = remember(items, displayQuery, onSearchQueryChange) {
        if (onSearchQueryChange != null) {
            items
        } else {
            items.filter { searchFilter(it, displayQuery.trim()) }
        }
    }

    val filteredFeaturedSections = remember(featuredSections, displayQuery, onSearchQueryChange) {
        if (onSearchQueryChange != null || !isSearching) {
            featuredSections
        } else {
            featuredSections.mapNotNull { section ->
                val matching = section.items.filter { searchFilter(it, displayQuery.trim()) }
                if (matching.isEmpty()) null else section.copy(items = matching)
            }
        }
    }

    val featuredKeys = remember(filteredFeaturedSections) {
        filteredFeaturedSections.flatMap { it.items }.map { itemKey(it) }.toSet()
    }

    val mainItemsExcludingFeatured = remember(filteredMainItems, featuredKeys) {
        filteredMainItems.filter { itemKey(it) !in featuredKeys }
    }

    val hasResults = filteredFeaturedSections.any { it.items.isNotEmpty() } ||
        mainItemsExcludingFeatured.isNotEmpty()

    val openDialog = {
        dialogSearchQuery = ""
        onSearchQueryChange?.invoke("")
        showDialog = true
    }

    val triggerColors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = openDialog,
            ),
    ) {
        OutlinedTextField(
            value = selectedItem?.let(itemLabel) ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = triggerColors,
        )
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = {
                showDialog = false
                dialogSearchQuery = ""
                onSearchQueryChange?.invoke("")
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    OutlinedTextField(
                        value = displayQuery,
                        onValueChange = { query ->
                            dialogSearchQuery = query
                            onSearchQueryChange?.invoke(query)
                        },
                        label = { Text("Search $label") },
                        placeholder = { Text("Type to search…") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(searchFocusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions.Default,
                    )

                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!hasResults) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp),
                        )
                    } else {
                        val featuredItems = filteredFeaturedSections.flatMap { it.items }
                        val showAllSection =
                            !isSearching &&
                                mainItemsExcludingFeatured.isNotEmpty() &&
                                featuredItems.isNotEmpty()

                        val listState = rememberLazyListState()
                        LaunchedEffect(listState, hasMoreItems, isLoadingMore, mainItemsExcludingFeatured.size) {
                            if (onLoadMore == null) return@LaunchedEffect
                            snapshotFlow {
                                val info = listState.layoutInfo
                                val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
                                lastVisible to info.totalItemsCount
                            }.collect { (lastVisible, total) ->
                                if (
                                    hasMoreItems &&
                                    !isLoadingMore &&
                                    total > 0 &&
                                    lastVisible >= total - 4
                                ) {
                                    onLoadMore()
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                        ) {
                            filteredFeaturedSections.forEach { section ->
                                if (section.items.isNotEmpty()) {
                                    item(key = "header-${section.title}") {
                                        Text(
                                            text = section.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 8.dp,
                                            ),
                                        )
                                    }
                                    items(
                                        section.items,
                                        key = { "featured-${section.title}-${itemKey(it)}" },
                                    ) { item ->
                                        val isLastFeaturedItem =
                                            showAllSection && item == featuredItems.last()
                                        DropdownResultRow(
                                            label = itemLabel(item),
                                            showDivider = !isLastFeaturedItem,
                                            onClick = {
                                                onItemSelected(item)
                                                showDialog = false
                                                dialogSearchQuery = ""
                                                onSearchQueryChange?.invoke("")
                                            },
                                        )
                                    }
                                }
                            }

                            if (showAllSection) {
                                item(key = "divider-before-all") {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                                item(key = "header-all") {
                                    Text(
                                        text = allSectionTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp,
                                        ),
                                    )
                                }
                            }

                            items(mainItemsExcludingFeatured, key = { itemKey(it) }) { item ->
                                DropdownResultRow(
                                    label = itemLabel(item),
                                    onClick = {
                                        onItemSelected(item)
                                        showDialog = false
                                        dialogSearchQuery = ""
                                        onSearchQueryChange?.invoke("")
                                    },
                                )
                            }

                            if (hasMoreItems || isLoadingMore) {
                                item(key = "load-more-footer") {
                                    Text(
                                        text = if (isLoadingMore) "Loading more…" else "Scroll for more",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 12.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownResultRow(
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean = true,
) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    if (showDivider) {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}
