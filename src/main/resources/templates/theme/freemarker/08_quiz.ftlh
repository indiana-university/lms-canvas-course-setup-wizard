<#-- Variables available: bannerImageAltText, bannerImageCssClasses, bannerImageUrl, courseId, headerCssClasses,
                          includeBannerImage, includeGuidance, includeNavigation, navigationCssClasses and wrapperCssClasses -->

<div id="dp-wrapper" class="dp-wrapper ${wrapperCssClasses}">
    <header class="dp-header ${headerCssClasses}">
        <h2 class="dp-heading"><span class="dp-header-pre"> <span class="dp-header-pre-1">Name </span> <span class="dp-header-pre-2">## </span> </span> <span class="dp-header-title">[Template] Quiz</span></h2>
    </header>

    <div class="dp-content-block">
        <#if includeGuidance>
        <div class="dp-action-item dp-action-item-block dp-action-item-instructions" style="display: none;" aria-hidden="true">
            <p>This quiz,&nbsp;<strong>[Template] Quiz</strong>, will be used to generate a quiz for each module in your course. This template won't actually be used in a module, so leave the template content<strong>&nbsp;</strong>as is unless you want to customize the default headings and prompts for all of your module quizzes.</p>
            <p>After you use the Module Builder utility to generate a complete set of modules for your course, use the instructions below to customize the quiz introduction page for the quiz in each module.</p>
            <p>To delete these instructions, click the green action items icon at the lower right ( <i class="dp-icon fas fa-tasks cp-bg-success dp-i-shape-rounded" aria-hidden="true"> </i>&nbsp;) to open the action items window. Then click the <strong>Remove&nbsp;</strong>button under the instruction block and close the action items window.</p>
        </div>
        </#if>
        <h3>Introduction</h3>
        <p>Enter introduction here.</p>
        <h3>Instructions</h3>
        <p>Enter instructions here.</p>
    </div>
</div>