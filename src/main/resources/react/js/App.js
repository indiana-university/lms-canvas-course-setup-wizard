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
