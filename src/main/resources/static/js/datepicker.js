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

/*
 *   This software or document includes material copied from or derived from https://www.w3.org/WAI/ARIA/apg/patterns/dialog-modal/examples/datepicker-dialog/.
 *   Copyright © 2023 World Wide Web Consortium. https://www.w3.org/Consortium/Legal/2023/doc-license"
 *
 */

'use strict';

var DatePickerDialog = function (cdp) {

  this.buttonLabelChange = 'Change Date';
  this.dayLabels = [
    'Sunday',
    'Monday',
    'Tuesday',
    'Wednesday',
    'Thursday',
    'Friday',
    'Saturday',
  ];
  this.monthLabels = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ];

  this.messageCursorKeys = '';
  this.lastMessage = '';

  this.textboxNode = cdp.querySelector('input[type="text"');
  this.buttonLabelChoose = "Choose date";
  var buttonLabel = this.textboxNode.labels[0];
  if (buttonLabel) {
    this.buttonLabelChoose += " for " + buttonLabel.innerHTML;
  }

  this.buttonNode = cdp.querySelector('.date-button');
  this.dialogNode = cdp.querySelector('[role="dialog"]');
  this.messageNode = this.dialogNode.querySelector('.dialog-message');

  this.monthYearNode = this.dialogNode.querySelector('.month-year');

  this.prevYearNode = this.dialogNode.querySelector('.prev-year');
  this.prevMonthNode = this.dialogNode.querySelector('.prev-month');
  this.nextMonthNode = this.dialogNode.querySelector('.next-month');
  this.nextYearNode = this.dialogNode.querySelector('.next-year');

  this.tbodyNode = this.dialogNode.querySelector('table.dates tbody');

  this.lastRowNode = null;

  this.days = [];

  this.focusDay = new Date();
  this.selectedDay = new Date(0, 0, 1);

  this.isMouseDownOnBackground = false;

  this.init();
};

DatePickerDialog.prototype.init = function () {
  this.textboxNode.addEventListener(
    'blur',
    this.setDateForButtonLabel.bind(this)
  );

  this.buttonNode.addEventListener(
    'keydown',
    this.handleButtonKeydown.bind(this)
  );
  this.buttonNode.addEventListener('click', this.handleButtonClick.bind(this));

  this.prevMonthNode.addEventListener(
    'click',
    this.handlePreviousMonthButton.bind(this)
  );
  this.nextMonthNode.addEventListener(
    'click',
    this.handleNextMonthButton.bind(this)
  );
  this.prevYearNode.addEventListener(
    'click',
    this.handlePreviousYearButton.bind(this)
  );
  this.nextYearNode.addEventListener(
    'click',
    this.handleNextYearButton.bind(this)
  );

  this.prevMonthNode.addEventListener(
    'keydown',
    this.handlePreviousMonthButton.bind(this)
  );
  this.nextMonthNode.addEventListener(
    'keydown',
    this.handleNextMonthButton.bind(this)
  );
  this.prevYearNode.addEventListener(
    'keydown',
    this.handlePreviousYearButton.bind(this)
  );
  this.nextYearNode.addEventListener(
    'keydown',
    this.handleNextYearButton.bind(this)
  );

  document.body.addEventListener(
    'mouseup',
    this.handleBackgroundMouseUp.bind(this),
    true
  );

  // Create Grid of Dates

  this.tbodyNode.innerHTML = '';
  for (var i = 0; i < 6; i++) {
    var row = this.tbodyNode.insertRow(i);
    this.lastRowNode = row;
    for (var j = 0; j < 7; j++) {
      var cell = document.createElement('td');

      cell.tabIndex = -1;
      cell.addEventListener('click', this.handleDayClick.bind(this));
      cell.addEventListener('keydown', this.handleDayKeyDown.bind(this));
      cell.addEventListener('focus', this.handleDayFocus.bind(this));

      cell.textContent = '-1';

      row.appendChild(cell);
      this.days.push(cell);
    }
  }

  this.updateGrid();
  this.close(false);
  this.setDateForButtonLabel();
};

DatePickerDialog.prototype.isSameDay = function (day1, day2) {
  return (
    day1.getFullYear() == day2.getFullYear() &&
    day1.getMonth() == day2.getMonth() &&
    day1.getDate() == day2.getDate()
  );
};

DatePickerDialog.prototype.isNotSameMonth = function (day1, day2) {
  return (
    day1.getFullYear() != day2.getFullYear() ||
    day1.getMonth() != day2.getMonth()
  );
};

DatePickerDialog.prototype.updateGrid = function () {
  var flag;
  var fd = this.focusDay;

  this.monthYearNode.textContent =
    this.monthLabels[fd.getMonth()] + ' ' + fd.getFullYear();

  var firstDayOfMonth = new Date(fd.getFullYear(), fd.getMonth(), 1);
  var dayOfWeek = firstDayOfMonth.getDay();

  firstDayOfMonth.setDate(firstDayOfMonth.getDate() - dayOfWeek);

  var d = new Date(firstDayOfMonth);

  for (var i = 0; i < this.days.length; i++) {
    flag = d.getMonth() != fd.getMonth();
    this.updateDate(this.days[i], flag, d, this.isSameDay(d, this.selectedDay));
    d.setDate(d.getDate() + 1);

    // Hide last row if all dates are disabled (e.g. in next month)
    if (i === 35) {
      if (flag) {
        this.lastRowNode.style.visibility = 'hidden';
      } else {
        this.lastRowNode.style.visibility = 'visible';
      }
    }
  }
};

DatePickerDialog.prototype.updateDate = function (
  domNode,
  disable,
  day,
  selected
) {
  var d = day.getDate().toString();
  if (day.getDate() <= 9) {
    d = '0' + d;
  }

  var m = day.getMonth() + 1;
  if (day.getMonth() < 9) {
    m = '0' + m;
  }

  domNode.tabIndex = -1;
  domNode.removeAttribute('aria-selected');
  domNode.setAttribute('data-date', day.getFullYear() + '-' + m + '-' + d);

  if (disable) {
    domNode.classList.add('disabled');
    domNode.textContent = '';
  } else {
    domNode.classList.remove('disabled');
    domNode.textContent = day.getDate();
    if (selected) {
      domNode.setAttribute('aria-selected', 'true');
      domNode.tabIndex = 0;
    }
  }
};

DatePickerDialog.prototype.moveFocusToDay = function (day) {
  var d = this.focusDay;

  this.focusDay = day;

  if (
    d.getMonth() != this.focusDay.getMonth() ||
    d.getFullYear() != this.focusDay.getFullYear()
  ) {
    this.updateGrid();
  }
  this.setFocusDay();
};

DatePickerDialog.prototype.setFocusDay = function (flag) {
  if (typeof flag !== 'boolean') {
    flag = true;
  }

  for (var i = 0; i < this.days.length; i++) {
    var dayNode = this.days[i];
    var day = this.getDayFromDataDateAttribute(dayNode);

    dayNode.tabIndex = -1;
    if (this.isSameDay(day, this.focusDay)) {
      dayNode.tabIndex = 0;
      if (flag) {
        dayNode.focus();
      }
    }
  }
};

DatePickerDialog.prototype.open = function () {
  this.dialogNode.style.display = 'block';
  this.dialogNode.style.zIndex = 500;

  this.getDateFromTextbox();
  this.updateGrid();
};

DatePickerDialog.prototype.isOpen = function () {
  return window.getComputedStyle(this.dialogNode).display !== 'none';
};

DatePickerDialog.prototype.close = function (flag) {
  if (typeof flag !== 'boolean') {
    // Default is to move focus to combobox
    flag = true;
  }

  this.setMessage('');
  this.dialogNode.style.display = 'none';

  if (flag) {
    this.buttonNode.focus();
  }
};

DatePickerDialog.prototype.moveToNextYear = function () {
  this.focusDay.setFullYear(this.focusDay.getFullYear() + 1);
  this.updateGrid();
};

DatePickerDialog.prototype.moveToPreviousYear = function () {
  this.focusDay.setFullYear(this.focusDay.getFullYear() - 1);
  this.updateGrid();
};

DatePickerDialog.prototype.moveToNextMonth = function () {
  this.focusDay.setMonth(this.focusDay.getMonth() + 1);
  this.updateGrid();
};

DatePickerDialog.prototype.moveToPreviousMonth = function () {
  this.focusDay.setMonth(this.focusDay.getMonth() - 1);
  this.updateGrid();
};

DatePickerDialog.prototype.moveFocusToNextDay = function () {
  var d = new Date(this.focusDay);
  d.setDate(d.getDate() + 1);
  this.moveFocusToDay(d);
};

DatePickerDialog.prototype.moveFocusToNextWeek = function () {
  var d = new Date(this.focusDay);
  d.setDate(d.getDate() + 7);
  this.moveFocusToDay(d);
};

DatePickerDialog.prototype.moveFocusToPreviousDay = function () {
  var d = new Date(this.focusDay);
  d.setDate(d.getDate() - 1);
  this.moveFocusToDay(d);
};

DatePickerDialog.prototype.moveFocusToPreviousWeek = function () {
  var d = new Date(this.focusDay);
  d.setDate(d.getDate() - 7);
  this.moveFocusToDay(d);
};

DatePickerDialog.prototype.moveFocusToFirstDayOfWeek = function () {
  var d = new Date(this.focusDay);
  d.setDate(d.getDate() - d.getDay());
  this.moveFocusToDay(d);
};

DatePickerDialog.prototype.moveFocusToLastDayOfWeek = function () {
  var d = new Date(this.focusDay);
  d.setDate(d.getDate() + (6 - d.getDay()));
  this.moveFocusToDay(d);
};

// Day methods

DatePickerDialog.prototype.isDayDisabled = function (domNode) {
  return domNode.classList.contains('disabled');
};

DatePickerDialog.prototype.getDayFromDataDateAttribute = function (domNode) {
  var parts = domNode.getAttribute('data-date').split('-');
  return new Date(parts[0], parseInt(parts[1]) - 1, parts[2]);
};
// Textbox methods

DatePickerDialog.prototype.setTextboxDate = function (domNode) {
  var d = this.focusDay;

  if (domNode) {
    d = this.getDayFromDataDateAttribute(domNode);
    // updated aria-selected
    this.days.forEach((day) =>
      day === domNode
        ? day.setAttribute('aria-selected', 'true')
        : day.removeAttribute('aria-selected')
    );
  }

  this.textboxNode.value =
    d.getMonth() + 1 + '/' + d.getDate() + '/' + d.getFullYear();
  this.setDateForButtonLabel();
};

DatePickerDialog.prototype.getDateFromTextbox = function () {
  var parts = this.textboxNode.value.split('/');
  var month = parseInt(parts[0]);
  var day = parseInt(parts[1]);
  var year = parseInt(parts[2]);

  if (
    parts.length === 3 &&
    Number.isInteger(month) &&
    Number.isInteger(day) &&
    Number.isInteger(year)
  ) {
    if (year < 100) {
      year = 2000 + year;
    }
    this.focusDay = new Date(year, month - 1, day);
    this.selectedDay = new Date(this.focusDay);
  } else {
    // If not a valid date (MM/DD/YY) initialize with todays date
    this.focusDay = new Date();
    this.selectedDay = new Date(0, 0, 1);
  }
};

DatePickerDialog.prototype.setDateForButtonLabel = function () {
  var parts = this.textboxNode.value.split('/');

  if (
    parts.length === 3 &&
    Number.isInteger(parseInt(parts[0])) &&
    Number.isInteger(parseInt(parts[1])) &&
    Number.isInteger(parseInt(parts[2]))
  ) {
    var day = new Date(
      parseInt(parts[2]),
      parseInt(parts[0]) - 1,
      parseInt(parts[1])
    );

    var label = this.buttonLabelChange;
    label += ', ' + this.dayLabels[day.getDay()];
    label += ' ' + this.monthLabels[day.getMonth()];
    label += ' ' + day.getDate();
    label += ', ' + day.getFullYear();
    this.buttonNode.setAttribute('aria-label', label);
  } else {
    // If not a valid date, initialize with "Choose Date"
    this.buttonNode.setAttribute('aria-label', this.buttonLabelChoose);
  }
};

DatePickerDialog.prototype.setMessage = function (str) {
  function setMessageDelayed() {
    this.messageNode.textContent = str;
  }

  if (str !== this.lastMessage) {
    setTimeout(setMessageDelayed.bind(this), 200);
    this.lastMessage = str;
  }
};

// Event handlers

DatePickerDialog.prototype.handleNextYearButton = function (event) {
  var flag = false;

  switch (event.type) {
    case 'keydown':
      switch (event.key) {
        case 'Esc':
        case 'Escape':
          this.close();
          flag = true;
          break;

        case 'Enter':
          this.moveToNextYear();
          this.setFocusDay(false);
          flag = true;
          break;
      }

      break;

    case 'click':
      this.moveToNextYear();
      this.setFocusDay(false);
      break;

    default:
      break;
  }

  if (flag) {
    event.stopPropagation();
    event.preventDefault();
  }
};

DatePickerDialog.prototype.handlePreviousYearButton = function (event) {
  var flag = false;

  switch (event.type) {
    case 'keydown':
      switch (event.key) {
        case 'Enter':
          this.moveToPreviousYear();
          this.setFocusDay(false);
          flag = true;
          break;

        case 'Tab':
//          if (event.shiftKey) {
//            this.okButtonNode.focus();
//            flag = true;
//          }
          break;

        case 'Esc':
        case 'Escape':
          this.close();
          flag = true;
          break;

        default:
          break;
      }

      break;

    case 'click':
      this.moveToPreviousYear();
      this.setFocusDay(false);
      break;

    default:
      break;
  }

  if (flag) {
    event.stopPropagation();
    event.preventDefault();
  }
};

DatePickerDialog.prototype.handleNextMonthButton = function (event) {
  var flag = false;

  switch (event.type) {
    case 'keydown':
      switch (event.key) {
        case 'Esc':
        case 'Escape':
          this.close();
          flag = true;
          break;

        case 'Enter':
          this.moveToNextMonth();
          this.setFocusDay(false);
          flag = true;
          break;
      }

      break;

    case 'click':
      this.moveToNextMonth();
      this.setFocusDay(false);
      break;

    default:
      break;
  }

  if (flag) {
    event.stopPropagation();
    event.preventDefault();
  }
};

DatePickerDialog.prototype.handlePreviousMonthButton = function (event) {
  var flag = false;

  switch (event.type) {
    case 'keydown':
      switch (event.key) {
        case 'Esc':
        case 'Escape':
          this.close();
          flag = true;
          break;

        case 'Enter':
          this.moveToPreviousMonth();
          this.setFocusDay(false);
          flag = true;
          break;
      }

      break;

    case 'click':
      this.moveToPreviousMonth();
      this.setFocusDay(false);
      flag = true;
      break;

    default:
      break;
  }

  if (flag) {
    event.stopPropagation();
    event.preventDefault();
  }
};

DatePickerDialog.prototype.handleDayKeyDown = function (event) {
  var flag = false;

  switch (event.key) {
    case 'Esc':
    case 'Escape':
      this.close();
      break;

    case ' ':
      this.setTextboxDate(event.currentTarget);
      flag = true;
      break;

    case 'Enter':
      this.setTextboxDate(event.currentTarget);
      this.close();
      flag = true;
      break;

    case 'Tab':
//      this.cancelButtonNode.focus();
      if (event.shiftKey) {
        this.nextYearNode.focus();
      }
      this.setMessage('');
      flag = true;
      break;

    case 'Right':
    case 'ArrowRight':
      this.moveFocusToNextDay();
      flag = true;
      break;

    case 'Left':
    case 'ArrowLeft':
      this.moveFocusToPreviousDay();
      flag = true;
      break;

    case 'Down':
    case 'ArrowDown':
      this.moveFocusToNextWeek();
      flag = true;
      break;

    case 'Up':
    case 'ArrowUp':
      this.moveFocusToPreviousWeek();
      flag = true;
      break;

    case 'PageUp':
      if (event.shiftKey) {
        this.moveToPreviousYear();
      } else {
        this.moveToPreviousMonth();
      }
      this.setFocusDay();
      flag = true;
      break;

    case 'PageDown':
      if (event.shiftKey) {
        this.moveToNextYear();
      } else {
        this.moveToNextMonth();
      }
      this.setFocusDay();
      flag = true;
      break;

    case 'Home':
      this.moveFocusToFirstDayOfWeek();
      flag = true;
      break;

    case 'End':
      this.moveFocusToLastDayOfWeek();
      flag = true;
      break;
  }

  if (flag) {
    event.stopPropagation();
    event.preventDefault();
  }
};

DatePickerDialog.prototype.handleDayClick = function (event) {
  if (!this.isDayDisabled(event.currentTarget) && event.which !== 3) {
    this.setTextboxDate(event.currentTarget);
    this.close();
  }

  event.stopPropagation();
  event.preventDefault();
};

DatePickerDialog.prototype.handleDayFocus = function () {
  this.setMessage(this.messageCursorKeys);
};

DatePickerDialog.prototype.handleButtonKeydown = function (event) {
  if (event.key === 'Enter' || event.key === ' ') {
    this.open();
    this.setFocusDay();

    event.stopPropagation();
    event.preventDefault();
  }
};

DatePickerDialog.prototype.handleButtonClick = function (event) {
  if (this.isOpen()) {
    this.close();
  } else {
    this.open();
    this.setFocusDay();
  }

  event.stopPropagation();
  event.preventDefault();
};

DatePickerDialog.prototype.handleBackgroundMouseUp = function (event) {
  if (
    !this.buttonNode.contains(event.target) &&
    !this.dialogNode.contains(event.target)
  ) {
    if (this.isOpen()) {
      this.close(false);
      event.stopPropagation();
      event.preventDefault();
    }
  }
};

// Initialize menu button date picker

window.addEventListener('load', function () {
  var datePickers = document.querySelectorAll('.datepicker');

  datePickers.forEach(function (dp) {
    new DatePickerDialog(dp);
  });
});
