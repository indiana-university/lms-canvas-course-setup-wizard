<#-- Variables available: bannerImageAltText, bannerImageCssClasses, bannerImageUrl, courseId, headerCssClasses,
                          includeBannerImage, includeGuidance, includeNavigation, navigationCssClasses and wrapperCssClasses -->

<div id="dp-wrapper" class="dp-wrapper ${wrapperCssClasses}">
    <header class="dp-header ${headerCssClasses}" style="margin-bottom: 0px;">
        <h2 class="dp-heading"><span class="dp-header-pre"> <span class="dp-header-pre-1">Name </span> <span class="dp-header-pre-2">## </span> </span> <span class="dp-header-title">[Template] Module Overview</span></h2>
    </header>
    <div class="dp-content-block">
        <#if includeGuidance>
        <div class="dp-action-item dp-action-item-block dp-action-item-instructions" style="display: none;" aria-hidden="true">
            <div class="dp-callout dp-callout-placeholder card dp-callout-position-default w-100 dp-callout-type-info dp-callout-color-dp-accent">
                <div class="dp-callout-side-emphasis"><i class="dp-icon fas fa-info-circle dp-default-icon">​</i></div>
                <div class="card-body">
                    <p class="card-text">This page, <strong>[Template] Module Page</strong>, will be used to generate an overview page for each module in your course. This template won't actually be used in a module, so leave the template content as is unless you want to customize the default headings and prompts for all of your module overview pages.</p>
                    <p class="card-text">If you use the Module Builder utility to generate a complete set of modules for your course, use the instructions below to customize the overview page for each module:</p>
                </div>
            </div>
            <p>To customize your module overview pages:</p>
            <ol class="dp-more-space" style="list-style-type: decimal;">
                <li>Delete the banner image or replace it with an image appropriate for the topic covered in the module.</li>
                <li>In the <strong>Introduction</strong> section, add a few sentences or paragraphs to introduce the module topic.</li>
                <li>In the <strong>Learning objectives</strong> section, replace the placeholder objectives with the learning objectives for this module.</li>
                <li>In the <strong>Readings and resources</strong> section, replace the placeholder readings with a list of the required and optional readings for this module. Where possible, create links to each item in the list.</li>
                <li>In the <strong>To-do</strong> section, replace the placeholder activities with a list of the content and activities in this module and their respective due dates.</li>
                <li>When you are finished customizing this page, if you want to delete these instructions, click the green action items icon at the lower right ( <i class="dp-icon fas fa-tasks cp-bg-success dp-i-shape-rounded" aria-hidden="true"> </i>) to open the action items window. Then click the&nbsp;<strong>Remove&nbsp;</strong>button under the instruction block and close the action items window.</li>
            </ol>
        </div>
        </#if>
        <h3 class="">Introduction</h3>
        <p>Enter_module_introduction_text</p>
        <h3 class="">Learning objectives</h3>
        <ul>
            <li>First objective</li>
            <li>Second objective</li>
        </ul>
        <h3 class="">Readings and resources</h3>
        <ul>
            <li>READ: item 1</li>
            <li>READ: item 2</li>
        </ul>
        <h3 class="">To-do</h3>
        <ul>
            <li>READ: Please complete the readings outlined above by:&nbsp;<strong>DATE</strong></li>
            <li>WATCH: Please watch and respond to the lecture video by:&nbsp;<strong>DATE</strong></li>
            <li>DISCUSS: Please complete this week's discussion by:&nbsp;<strong>DATE</strong></li>
            <li>SUBMIT: Please submit this week's assignment by:&nbsp;<strong>DATE</strong></li>
        </ul>
    </div>
</div>