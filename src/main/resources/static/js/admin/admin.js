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

    $("#success-alert, #error-alert").not(".rvt-display-none").focus();

    $("#add-banner-category").click(function(event) {
        $("#banner-category-title").text("Add Banner Image Category");
        $("#category-name").val('');
        $("#category-id").val('');
        $("#category-name").focus();
    });

    $("#add-banner-category-inline").click(function(event) {
        $('#category-dialog-error').addClass('rvt-display-none');
        $("#category-name-inline").val('');
        $("#category-name-inline").focus();
    });

    $(".edit-category").click(function(event) {
        $("#banner-category-title").text("Edit Banner Image Category");
        let categoryName = $(this).data("category-name");
        let categoryId = $(this).data("category-id");

        $("#category-name").val(categoryName);
        $("#category-id").val(categoryId);

        $("#category-name").focus();
    });

    $("#enable-feature").click(function(event) {
        $("#account-id").focus();
    })

    $(".feature-delete").click(function(event) {
        let featureName = $(this).data("delete-feature-name");
        let id = $(this).data("delete-id");
        let featureId = $(this).data("delete-feature-id");
        let accountId = $(this).data("delete-feature-account");

        $("#delete-feature-name").text(featureName);
        $("#delete-feature-id").text(featureId);
        $("#delete-account-id").text(accountId);

        $("#delete-id-input").val(id);
    });

    $(".delete-content").click(function(event) {
        let contentName = $(this).data("content-name");
        $("#delete-content-name").text(contentName);
        $("#delete-content-input").val(contentName);
    });

    $(".upload-content").click(function(event) {
        let content = $(this).data("content-name");
        $("#content-name").text(content);
        $("#theme-content-id").val(content);
    });

    $(".create-category-from-banner").click(function(event) {
        let categoryInput = $('#category-name-inline');
        let valid = handleValidation(categoryInput);

        if (valid) {
            let submitUrl = $(this).data("submiturl");
            createBannerCategory(submitUrl, categoryInput.val());
            applyLoadingButton($(this).get(0));
        }
    });

    // dialog needs to be separate because we don't want to validate hidden dialog fields on the main screen or validate the main screen when we are in a dialog
    $(".validate-not-empty, .validate-dialog").click(function(event) {
        event.preventDefault();

        let valid = true;
        let validatedInputs;
        let submitButton = $(this);
        if (submitButton.hasClass("validate-not-empty")) {
            validatedInputs = $('.required-input');
        } else {
            validatedInputs = $('.required-input-dialog');
        }

        valid = handleValidation(validatedInputs);

        if (valid) {
            applyLoadingButton(submitButton.get(0));
        }

        return valid;
    });

});

function handleValidation(validatedInputs) {
    let valid = true;
    validatedInputs.each(function (event) {
        let currInput = $(this);
        let errorId = currInput.data("error-id");

        let isVisible = currInput.is(':visible');
        let isEmptyText = !currInput.val();
        let isEmptySelect = currInput.is('select') && (currInput.val() == "" || currInput.val() == 0);
        let isMissingFile = currInput.is(':file') && currInput.get(0).files.length === 0;

        let isInvalidDate = false;
        if(currInput.hasClass('validate-date')) {
            const inputDate = new Date(currInput.val());
            const today = new Date();
            if (isNaN(inputDate) || inputDate < today) {
                isInvalidDate = true;
            }
        }

        if (isVisible && (isEmptyText || isEmptySelect || isInvalidDate || isMissingFile)) {
            invalidInput(currInput, errorId);
            valid = false;
        } else {
            // remove any old validation since it is valid now
            validInput(currInput, errorId);
        }
    });

    if (!valid) {
        $('[aria-invalid="true"]').first().focus();
    }

    return valid;
}

function invalidInput(currInput, errorId) {
    currInput.attr({
        "aria-describedby": errorId,
        "aria-invalid": "true"
    })

    $('#' + errorId).removeClass("rvt-display-none");
}

function validInput(currInput, errorId) {
    currInput.removeAttr('aria-describedby aria-invalid');
    $('#' + errorId).addClass("rvt-display-none");
}

function resetDialogValidation() {
    $('.required-input-dialog').each(function () {
        let currInput = $(this);
        let errorId = currInput.data("error-id");
        validInput(currInput, errorId);
    });
}

// Ensure any old validation is reset when a dialog is opened
document.addEventListener('rvtDialogOpened', function (event) {
    resetDialogValidation();
})

document.querySelector('[data-rvt-dialog="enable-feature-dialog"]')?.addEventListener('rvtDialogOpened', function (event) {
   document.getElementById('enable-feature-form').reset();
})

async function createBannerCategory(submitUrl, categoryName) {
    let token = $('#_csrf').attr('content');
    let header = $('#_csrf_header').attr('content');

    const myHeaders = new Headers();
    myHeaders.append(header, token);
    myHeaders.append('Content-Type', 'application/json');

    const response = await fetch(submitUrl, {
        method: "POST",
        headers: myHeaders,
        body: JSON.stringify({
             active: 'true',
             name: categoryName
          })
    });

    if (response.ok) {
        const newCategoryId = await response.json();
        updateCategoryList(newCategoryId, categoryName);

    } else {
        console.log("Error attempting to save new banner category.");
        console.log(response.message);
        $('#category-dialog-error').removeClass('rvt-display-none');
        resetLoading(document.getElementById('save-category-inline-confirm'));
    }
}

async function updateCategoryList(newCategoryId, newCategoryName) {
    let token = $('#_csrf').attr('content');
    let header = $('#_csrf_header').attr('content');

    let categorySelect = $("#category-select");
    let refreshUrl = categorySelect.data("refreshurl");
    let selectedCategories = categorySelect.val();
    selectedCategories.push(newCategoryId);

    const myHeaders = new Headers();
    myHeaders.append(header, token);
    myHeaders.append('Content-Type', 'application/json');

    const response = await fetch(refreshUrl, {
        method: "GET",
        headers: myHeaders
    });

    if (response.ok) {

        let categoryArray = await response.json();

        // rebuild the select to include the new category
        categorySelect.empty();

        $.each(categoryArray, function(index, item) {
            let selected = contains(selectedCategories, item.id);
            categorySelect.append(new Option(item.name, item.id, false, selected));
        });

        resetLoading(document.getElementById('save-category-inline-confirm'));
        document.getElementById('sr-announcement').textContent = "Banner image category " + newCategoryName + " was created and selected as a category for this banner image. Save your changes to keep this category association.";

        // close the dialog
        const categoryDialog = document.querySelector('[data-rvt-dialog="banner-category-inline-dialog"]');
        categoryDialog.close();


    } else {
        console.log("Error attempting to retrieve the banner categories.");
        console.log(response.message);
    }
}

// contains method that treats the string and numeric value of a number as equivalent "1" = 1
// needed since some of the category ids are retrieved from the html and some from the controller
function contains(array, value) {
  for (let element of array) {
    if (element == value) {
      return true;
    }
  }
  return false;
}


// Customize a few of the search input related wrapper classes
DataTable.ext.classes.search.input = 'rvt-m-left-xs';
DataTable.ext.classes.search.container = 'rvt-p-top-md search-wrapper';

// DataTables sorting defaults to third click removing sorting. This sets it to asc/desc only
DataTable.defaults.column.orderSequence = ['asc', 'desc'];

DataTable.render.ellipsis = function () {
    return function ( data, type, row ) {
        return type === 'display' && data.length > 200 ?
            data.substr( 0, 200 ) +'…' :
            data;
    }
};

// Uses filters
$('#featureTable').DataTable({
   orderCellsTop: true,
   paging: false,
   order: [[0, 'asc']],
   language: {
       // Setting the text for the search label, mostly to remove the colon that is there by default
       search: 'Search',
       select: {
          aria: {
          }
       }
   },
   columnDefs: [
        {
            targets: ['.colActions'],
            orderable: false
        },
        {
            // Enabling filters for these columns
            targets: ['.colFeatureName', '.colFeatureId', '.colCategory', '.colAccount'],
            lmsFilters: true
        }
       ],
   initComplete: function () {
       $('#appTable').wrap("<div style='overflow:auto;width:100%;position:relative;'></div>");
       $('.search-wrapper label').addClass('rvt-label rvt-ts-16');
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

// sort ascending, active filter
$('#bannerTable, #bannerCategoryTable').DataTable({
   orderCellsTop: true,
   paging: false,
   order: [[0, 'asc']],
   language: {
       // Setting the text for the search label, mostly to remove the colon that is there by default
       search: 'Search',
       select: {
          aria: {
          }
       }
   },
   columnDefs: [
        {
            targets: ['.colActions'],
            orderable: false
        },
        {
            // Enabling filters for these columns
            targets: ['.colActive, .colCategory'],
            lmsFilters: true
        },
        {
            targets: ['.colActive'], visible: false
        }
       ],
   initComplete: function () {
       $('#appTable').wrap("<div style='overflow:auto;width:100%;position:relative;'></div>");
       $('.search-wrapper label').addClass('rvt-label rvt-ts-16');
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

// Sorted ascending, no filters
$('#themeTable, #themeContentTable, #popupTable').DataTable({
   orderCellsTop: true,
   paging: false,
   order: [[0, 'asc']],
   language: {
       // Setting the text for the search label, mostly to remove the colon that is there by default
       search: 'Search',
       select: {
          aria: {
          }
       }
   },
   columnDefs: [
        {
            targets: ['.colActions'],
            orderable: false
        },

        {
            targets: ['.colDates'],
            type: 'date'
        },
        {
            targets: ['.colNotes'],
            render: DataTable.render.ellipsis()
        }
       ],
   initComplete: function () {
       $('#appTable').wrap("<div style='overflow:auto;width:100%;position:relative;'></div>");
       $('.search-wrapper label').addClass('rvt-label rvt-ts-16');
       $('.search-wrapper').addClass('-rvt-m-bottom-sm rvt-p-top-none');
   }
});
