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
              <label className="rvt-label rvt-ts-18" id="course-select-label" htmlFor="course-select-input">Import from course <span class="rvt-sr-only">(Required)</span></label>
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
        return null;
    }
  }
}

export default CourseSelect
