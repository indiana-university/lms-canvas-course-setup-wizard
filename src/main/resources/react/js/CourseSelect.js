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
import React, {useState} from 'react';
import {useTreeData} from 'react-stately';
import {ComboBox, lightTheme, Item, Provider} from '@adobe/react-spectrum';

function CourseSelect(props) {
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
           <Provider theme={lightTheme}>
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
           </Provider>
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

export default CourseSelect
