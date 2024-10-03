<#-- Variables available: bannerImageAltText, bannerImageCssClasses, bannerImageUrl, courseId, headerCssClasses,
                          includeBannerImage, includeGuidance, includeNavigation, navigationCssClasses and wrapperCssClasses -->

<div id="dp-wrapper" class="dp-wrapper ${wrapperCssClasses}" <#if includeNavigation> data-nav-class="container-fluid dp-link-grid ${navigationCssClasses}"</#if> >
    <header class="dp-header ${headerCssClasses}">
        <h2 class="dp-heading"><span class="dp-header-pre"><span class="dp-header-pre-1">Enter SIS ID</span> </span> <span class="dp-header-title">Enter your Course Title</span></h2>
    </header>
    <#if includeBannerImage>
    <div class="dp-banner-image"><img class="${bannerImageCssClasses}}" role="presentation" src="${bannerImageUrl}" alt="${bannerImageAltText}" data-name="typewriter_five_ws.png" data-size="478 KB" data-available-range="false" data-hidden="false" data-file-id="165239671" data-filename="typewriter_five_ws.png" data-locked="false" /></div>
    </#if>
    <#if includeNavigation>
    <nav class="container-fluid dp-link-grid ${navigationCssClasses}">
        <ul class="row">
            <li class="col-sm-12 col-md-6 col-lg-3"><a> Start Here</a></li>
            <li class="col-sm-12 col-md-6 col-lg-3"><a href="/courses/${courseId}/assignments/syllabus"> Syllabus</a></li>
            <li class="col-sm-12 col-md-6 col-lg-3"><a href="/courses/${courseId}/modules"> Modules</a></li>
            <li class="col-sm-12 col-md-6 col-lg-3"><a href="/courses/${courseId}/pages/more-resources"> Resources</a></li>
        </ul>
    </nav>
    </#if>
    <#if includeGuidance>
    <div class="dp-action-item dp-action-item-block dp-action-item-instructions" style="display: none;">
        <p>For more information on how to customize this home page, visit Customize Your Home Page.</p>
        <p>To get started customizing this page:</p>
        <ol id="" class="">
            <li style="text-align: left;"><span>To add your course number or ID to the top of the page, do not try to edit the text directly.&nbsp; Instead, follow this steps:</span>
                <ol class="dp-more-space" style="list-style-type: lower-alpha;">
                    <li style="text-align: left;"><span>Click the green action items icon at the lower right ( <i class="dp-icon fas fa-tasks cp-bg-success dp-i-shape-rounded" aria-hidden="true"></i>&nbsp;) to open the action items window.&nbsp; &nbsp;</span></li>
                    <li style="text-align: left;"><span>In the first input prompt, enter your course number or ID in the text box.&nbsp; Then click <strong>replace prompt </strong>button&nbsp;(&nbsp;<i class="fas fa-file-import fa-flip-horizontal cp-txt-dp-white" aria-hidden="true"></i>&nbsp;) to replace the yellow prompt text on the page with your response.</span></li>
                    <li style="text-align: left;"><span>In the second input prompt, enter your course title in the text box.&nbsp; Then click <strong>replace prompt </strong>button&nbsp;(&nbsp;<i class="fas fa-file-import fa-flip-horizontal cp-txt-dp-white" aria-hidden="true"></i>&nbsp;) to replace the yellow prompt text on the page with your response.</span></li>
                    <li style="text-align: left;"><span>Close the action items window.</span></li>
                </ol>
            </li>
            <li style="text-align: left;"><span>Replace the placeholder banner image or delete it.&nbsp; For instructions, see <a class="dp-course-link" href="https://iu.test.instructure.com/courses/2227739/pages/add-a-custom-banner-image">Add a Custom Banner Image.</a></span></li>
            <li style="text-align: left;"><span>Replace the placeholder text "Enter_the_course_introduction" below these instructions with a couple of paragraphs to introduce the course to your students.</span></li>
            <li style="text-align: left;">In the <strong>To begin the course </strong>section, revise or replace the sample text with tasks you would like to complete right away. Be sure to delete content that does not apply to your course.</li>
            <li style="text-align: left;">In the <strong>Meet your instructor </strong>section, write a few sentences to introduce yourself.&nbsp; You may also want to consider adding a photo or a welcome video.&nbsp; <br /><br />Then, fill in your contact information and office hours. Let your students know your preferred contact method, typical turnaround time on questions, and how to contact you in emergencies.&nbsp; For example:
                <p>&nbsp;</p>
                <blockquote>
                    <p><strong>Email:</strong> <em> <strong>Canvas e-mail</strong> is my preferred means of contact. I will return your e-mails within 48 business hours. For emergencies only, contact me at <a href="mailto:johndoe@iu.edu" target="_blank" rel="noopener">johndoe@iu.edu</a> and use the subject line to describe the nature of the emergency.</em></p>
                </blockquote>
            </li>
            <li style="text-align: left;"><span>In the <strong>Need help? </strong>section, revise the listed resources as needed.&nbsp; For example, if your course does not use an eText, remove the third bullet point. Also, if there are campus- or course-specific resources or services that you want your students to know about, add them in the list.&nbsp;&nbsp;</span><span><br /></span></li>
            <li style="text-align: left;"><span>When you are finished customizing this page</span>, if you want to delete these instructions, c<span>lick the green action items icon at the lower right (<span>&nbsp;</span><i class="dp-icon fas fa-tasks cp-bg-success dp-i-shape-rounded" aria-hidden="true"></i>&nbsp;) to open the action items window. Then click the&nbsp;<strong>Remove&nbsp;</strong>button under the instruction block.</span></li>
        </ol>
    </div>
    </#if>
    <h3>Introduction</h3>
    <p>Enter_the_course_introduction</p>
    <h3>To begin this course</h3>
    <p>Here are some tasks to get you started right away:</p>
   <ol class="dp-more-space">
        <li>Review the remaining content on this page.</li>
        <li>Read the &nbsp<a href="/courses/2227484/assignments/syllabus">syllabus</a> - located in the course navigation menu at the left.</li>
        <li>Read and complete the activities in the <a class="dp-course-link" href="https://iu.test.instructure.com/courses/2227484/modules/4205999">Getting Started</a> module.</span></li>
        <li>Set up the technology you'll need (use resources below for help).</li>
        <li>Gather the required materials:</li>
    </ol>
    <p>Purchase the following textbook(s):</p>
    <ul>
        <li style="list-style-type: none;">
            <ul>
                <li>AUTHOR.&nbsp; (DATE).&nbsp;&nbsp;<em>TITLE</em><span>. &nbsp;Xth Ed.&nbsp; PUBLISHER.&nbsp; (ISBN&nbsp; XXX-X-XXXX-X)</span></li>
                <li>AUTHOR.&nbsp; (DATE).&nbsp;&nbsp;<em>TITLE</em><span>. &nbsp;Xth Ed.&nbsp; PUBLISHER.&nbsp; (ISBN&nbsp; XXX-X-XXXX-X)</span>&nbsp; &nbsp;</li>
            </ul>
        </li>
    </ul>
    <p style="padding-left: 40px;">As noted in the Schedule of Classes, we will be using an IU eText in this class. For more information, please see: "<a class="inline_disabled external" href="http://expand.iu.edu/browse/etexts/courses/the-student-guide-to-iu-etexts" target="_blank" rel="noopener"><span>The Student Guide to IU eTexts</span><span class="external_link_icon" role="presentation"><span class="screenreader-only">Links to an external site.</span></span></a>&rdquo;. &nbsp;</p>
    <ul>
        <li style="list-style-type: none;">
            <ul>
                <li>Please familiarize yourself with the etext, which is available in <strong>IU eTexts (Unizin Engage)</strong> tool in the course navigation menu.</li>
                <li>Links to the required courseware and tools are provided in [give location]</li>
                <li>I will be providing you with an code for accessing the courseware for this course.</li>
            </ul>
        </li>
    </ul>
    <h3 class="" style="text-align: center;">Meet your instructor</h3>
    <p style="text-align: left;">Write_your_self_introduction_here</p>
    <p style="text-align: left;"><strong>Email:</strong>&nbsp;</p>
    <p style="text-align: left;"><strong>Phone:&nbsp;</strong></p>
    <p style="text-align: left;"><strong>Office location:&nbsp;</strong></p>
    <p style="text-align: left;"><strong>Office Hours:</strong>&nbsp;</p>
    <h3>Need help?</h3>
    <ul>
        <li style="text-align: left;"><span><span style="color: #000000;">Visit the</span> <strong><a class="" href="https://learning.iu.edu/">Learning.IU website</a></strong></span><span style="color: #000000;"> for information and resources about learning at IU.</span></li>
        <li style="text-align: left;"><a class="" href="https://servicenow.iu.edu/kb?id=kb_article_view&sysparm_article=KB0023963" target="_blank" rel="noopener"><span><strong>UITS Tech Support:</strong></span></a><span style="color: #000000;"> Open 24/7 to help with your technology questions.</span></li>
        <li style="text-align: left;"><span style="color: #000000;">Enroll yourself in <strong><a class="inline_disabled dp-ext-ignore" href="https://iu.test.instructure.com/courses/1683358" target="_blank" rel="noopener">The Student Guide to IU eTexts</a></strong> to learn all the basics about the initiative, and view tutorials for using Engage.</span></li>
    </ul>
</div>