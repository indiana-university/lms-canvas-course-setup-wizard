import React from 'react'
import styled from 'styled-components'
import axios from 'axios'
import CourseSelect from 'CourseSelect'

class App extends React.Component {
    /**
     * Initialization stuff
     */
    constructor() {
        super()

        // Set the x-auth-token head for all requests
        // The customId value got injected in to the react.html file and is a global variable
        axios.defaults.headers.common['X-Auth-Token'] = customId;
        axios.defaults.headers.common[csrfHeaderName] = csrfValue;
//        axios.interceptors.request.use(request => {
//            console.debug('Starting Request', request)
//            return request
//        })

        this.state = {
            courses: []
        }

    }

    /**
     * Call off to the REST endpoints to load data
     */
    componentDidMount() {
        var self = this;
        axios.all([getCourses()])
            .then(axios.spread(function (courses) {
                self.setState({
                    courses: courses.data
                });
            }))
            .catch(error => {
                alert(error);
            });
    }

    /**
     * Render
     */
    render() {

        return (
            <CourseSelect courses={this.state.courses} selectedCourseId={selectedCourseId} />
        );
    }

}

function getCourses() {
    return axios.get(`/tool/${courseId}/courses`);
}

export default App
