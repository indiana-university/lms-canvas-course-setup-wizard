/*-
 * #%L
 * course-setup-wizard
 * %%
 * Copyright (C) 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

jQuery(document).ready(function($) {

    $("#add-banner-category").click(function(event) {
        $("#banner-category-title").text("Add Banner Image Category");
        $("#category-name").val('');
        $("#category-id").val('new');
    });

    $(".edit-category").click(function(event) {
        $("#banner-category-title").text("Edit Banner Image Category");
        let categoryName = $(this).data("category-name");
        let categoryId = $(this).data("category-id");

        $("#category-name").val(categoryName);
        $("#category-id").val(categoryId);
    });

    $(".feature-delete").click(function(event) {
        let feature = $(this).data("delete-feature");
        let accountId = $(this).data("delete-account");

        $("#deleted-item").text(feature);
        $("#deleted-post-text").text(" for account " + accountId);
    });

    $(".upload-content").click(function(event) {
        let content = $(this).data("content-name");
        $("#theme-content-name").text(content);
    });

});


// Customize a few of the search input related wrapper classes
DataTable.ext.classes.search.input = 'rvt-m-left-xs';
DataTable.ext.classes.search.container = 'rvt-p-top-md search-wrapper';

// DataTables sorting defaults to third click removing sorting. This sets it to asc/desc only
DataTable.defaults.column.orderSequence = ['asc', 'desc'];

var table = $('#featureTable').DataTable({
   orderCellsTop: true,
   paging: false,
   order: [[1, 'asc']],
   language: {
       // Setting the text for the search label, mostly to remove the colon that is there by default
       search: 'Search',
       select: {
          aria: {

          }
       }
   },
    lmsAlly: {
        checkLabelTargetSelector: 'td.displayName'
    },
   columnDefs: [
        {
            targets: ['.colActions'],
            orderable: false
        },
        {
            // Enabling filters for these columns
            targets: ['.colFeature', '.colCategory', '.colAccount'],
            lmsFilters: true
        }
       ],
   initComplete: function () {
       $('#appTable').wrap("<div style='overflow:auto;width:100%;position:relative;'></div>");
       $('.search-wrapper label').addClass('rvt-label rvt-ts-16');
   },
   select: {
        selector: 'th:first-child',
        style: 'multi',
        info: false
   },
   layout: {
       topStart: {
           // Configuration for the filters
           lmsFilters: {
               containerClass: 'rvt-flex-md-up rvt-p-top-md',
               includeClearFilters: true
           }
       },
   }
});