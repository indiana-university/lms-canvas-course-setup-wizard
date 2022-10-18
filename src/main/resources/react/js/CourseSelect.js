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
import React from 'react';
import Select from 'react-select';

class CourseSelect extends React.Component {

  constructor(props) {
    super(props);
  }

  findSelectedOption = (courses, selCourseId) => {
    //const selCourseId = this.props.selectedCourseId
    //const courses = this.props.courses
    console.log(`selected id:`, selCourseId)
    if (selCourseId != null) {
        const option = courses.filter((c) => {
            if (c.value === selCourseId) {
                return c
            }
        });
        console.log(`Default option:`, option)
        return option
    }
    return null
  };

  render() {
    const courses = this.props.courses;

    if (courses.length > 0) {
        return (
          // There "should" be an attribute in the component below for className="rvt-select", but rivet really doesn't play
          // nice with any other styles and it looks dumb.  So, just using the component's defaults.
          // Also             value={selectedOption}
          <>
              <label className="rvt-label rvt-ts-18" id="course-select-label" htmlFor="course-select-input">Import from course <span className="rvt-sr-only sr-spacing"> Required</span></label>
              <Select
                defaultValue={this.findSelectedOption(courses, this.props.selectedCourseId)}
                options={courses}
                aria-labelledby="course-select-label"
                inputId="course-select-input"
                name="selectedCourseId"
                placeholder="Select a course"
              />
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
}

export default CourseSelect
