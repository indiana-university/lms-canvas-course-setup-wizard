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
import React, {useState} from "react";
import { Item, useComboBoxState, useTreeData } from "react-stately";
import { mergeProps, useButton, useComboBox, useFilter, useListBox, useOption, useOverlay, DismissButton } from "react-aria";

function ComboBox(props) {
  // Get a basic "contains" filter function for input value
  // and option text comparison
  let { contains } = useFilter({ sensitivity: "base" });

  // Create state based on the incoming props and the filter function
  let state = useComboBoxState({ ...props, defaultFilter: contains });

  let triggerRef = React.useRef();
  let inputRef = React.useRef();
  let listBoxRef = React.useRef();
  let popoverRef = React.useRef();

  // Get props for child elements from useComboBox
  let {
    buttonProps: triggerProps,
    inputProps,
    listBoxProps,
    labelProps
  } = useComboBox(
    {
      ...props,
      inputRef,
      buttonRef: triggerRef,
      listBoxRef,
      popoverRef,
      menuTrigger: "input"
    },
    state
  );

  // Get props for the trigger button based on the
  // button props from useComboBox
  let { buttonProps } = useButton(triggerProps, triggerRef);

  return (
    <div>
      <label className="rvt-label rvt-display-block rvt-ts-18" {...labelProps}>{props.label}</label>
      <div
      style={{ position: "relative" }}
      className="rvt-display-inline-block rvt-width-4-xl">
        <div className="rvt-input-group">
            <input
              {...inputProps}
              ref={inputRef}
              className="rvt-text-input rvt-input-group__input"
            />
            <div className="rvt-input-group__append">
                <button
                  {...buttonProps}
                  ref={triggerRef}
                  className="rvt-button"
                >
                    <rvt-icon name="chevron-down"></rvt-icon>
                </button>
            </div>
        </div>
        {state.isOpen && (
          <ListBoxPopup
            {...listBoxProps}
            // Use virtual focus to get aria-activedescendant tracking and
            // ensure focus doesn't leave the input field
            shouldUseVirtualFocus
            shouldFocusOnHover
            shouldSelectOnPressUp
            listBoxRef={listBoxRef}
            popoverRef={popoverRef}
            state={state}
            className="rvt-bg-white"
          />
        )}
      </div>
    </div>
  );
}

function ListBoxPopup(props) {
  let {
    popoverRef,
    listBoxRef,
    state,
    shouldUseVirtualFocus,
    shouldFocusOnHover,
    shouldSelectOnPressUp,
    ...otherProps
  } = props;

  // Get props for the list box.
  // Prevent focus moving to list box via shouldUseVirtualFocus
  let { listBoxProps } = useListBox(
    {
      autoFocus: state.focusStrategy,
      disallowEmptySelection: true,
      shouldUseVirtualFocus,
      shouldFocusOnHover,
      shouldSelectOnPressUp,
      ...otherProps
    },
    state,
    listBoxRef
  );

  // Handle events that should cause the popup to close,
  // e.g. blur, clicking outside, or pressing the escape key.
  let { overlayProps } = useOverlay(
    {
      onClose: () => state.close(),
      shouldCloseOnBlur: true,
      isOpen: state.isOpen,
      isDismissable: true
    },
    popoverRef
  );

  // Add a hidden <DismissButton> component at the end of the list
  // to allow screen reader users to dismiss the popup easily.
  return (
    <div {...overlayProps} ref={popoverRef}>
      <ul
        {...mergeProps(listBoxProps, otherProps)}
        ref={listBoxRef}
        style={{
          position: "absolute",
          width: "100%",
          margin: "4px 0 0 0",
          padding: 0,
          listStyle: "none",
          border: "1px solid gray",
          zIndex: "9999"
        }}
      >
        {[...state.collection].map((item) => (
          <Option
            shouldUseVirtualFocus
            shouldFocusOnHover
            shouldSelectOnPressUp
            key={item.key}
            item={item}
            state={state}
          />
        ))}
      </ul>
      <DismissButton onDismiss={() => state.close()} />
    </div>
  );
}

function Option({ item, state, shouldUseVirtualFocus, shouldFocusOnHover, shouldSelectOnPressUp }) {
  let ref = React.useRef();
  let isDisabled = state.disabledKeys.has(item.key);
  let isSelected = state.selectionManager.isSelected(item.key);
  // Track focus via focusedKey state instead of with focus event listeners
  // since focus never leaves the text input in a ComboBox
  let isFocused = state.selectionManager.focusedKey === item.key;

  // Get props for the option element.
  // Prevent options from receiving browser focus via shouldUseVirtualFocus.
  let { optionProps } = useOption(
    {
      key: item.key,
      isDisabled,
      isSelected,
      shouldSelectOnPressUp,
      shouldFocusOnHover,
      shouldUseVirtualFocus
    },
    state,
    ref
  );

  let backgroundColor;
  let color = "black";

  if (isSelected) {
    backgroundColor = "#0066cc";
    color = "white";
  } else if (isFocused) {
    backgroundColor = "lightGray";
  } else if (isDisabled) {
    backgroundColor = "transparent";
    color = "lightGray";
  }

  return (
    <li
      {...optionProps}
      ref={ref}
      style={{
        background: backgroundColor,
        color: color,
        padding: "5px 7px",
        outline: "none",
        cursor: "pointer"
      }}
    >
      {item.rendered}
    </li>
  );
}

export default function CourseSelect(props) {

  let options = props.courses;

  let list = useTreeData({
    initialItems: options
  });

  let [fieldState, setFieldState] = React.useState({
    selectedKey: props.selectedCourseId ?? '',
    inputValue: list.getItem(props.selectedCourseId)?.value.name ?? ''
  });

  let onSelectionChange = (key) => {
    setFieldState({
      inputValue: list.getItem(key)?.value.name ?? '',
      selectedKey: key
    });

    document.getElementById('selectedCourseId').value = key;
  };

  let onInputChange = (value) => {
    setFieldState((prevState) => ({
      inputValue: value,
      selectedKey: value === '' ? null : prevState.selectedKey
    }));
  };

  if (options.length > 0) {
        return (
        <>
           <ComboBox
               label="Import from course"
                defaultItems={list.items}
                selectedKey={fieldState.selectedKey}
                inputValue={fieldState.inputValue}
                onSelectionChange={onSelectionChange}
                onInputChange={onInputChange}
                isRequired
                necessityIndicator="label"
                width="100%"
                name="courseSelectInput"
                direction="bottom"
                shouldFocusWrap
           >
               {(item) => <Item>{item.value.name}</Item>}
           </ComboBox>
         <input type="hidden" id="selectedCourseId" name="selectedCourseId" />
       </>
      );
    } else {
        return (
            <span>
                You are not listed as a Teacher, TA, or Designer in any other courses. If you would like to import
                content from a colleague's course, please work with them or your department administrator to add you to
                the course in one of these roles.
            </span>
        );
    }
}
