<#-- Variables available: bannerImageAltText, bannerImageCssClasses, bannerImageUrl, courseId, headerCssClasses,
                          includeBannerImage, includeGuidance, includeNavigation, navigationCssClasses and wrapperCssClasses -->

<div id="dp-wrapper" class="dp-wrapper ${wrapperCssClasses}">
    <header class="dp-header ${headerCssClasses}">
        <h2 class="dp-heading"><span class="dp-header-pre"><span class="dp-header-pre-1">Name </span> <span class="dp-header-pre-2">## </span> </span> <span class="dp-header-title">[Template] Page</span></h2>
    </header>
    <div class="dp-content-block">
        <#if includeGuidance>
        <div class="dp-action-item dp-action-item-block dp-action-item-instructions" style="display: none;" aria-hidden="true">
            <ol style="list-style-type: decimal;">
                <li class="dp-action-item dp-action-item-block dp-action-item-instructions" style="display: none;" aria-hidden="true">
                    <div class="dp-callout dp-callout-placeholder card dp-callout-position-default w-100 dp-callout-type-info dp-callout-color-dp-accent">
                        <div class="dp-callout-side-emphasis"><i class="dp-icon fas fa-info-circle dp-default-icon">​</i></div>
                        <div class="card-body">
                            <p class="card-text">Use this template to create additional content pages for each of your modules. To add pages to your modules, add this page, <strong>[Template] Page</strong>, to the desired position(s) within the <em>module pattern</em> when you use the Module Builder utility. A copy of this page will be added to each module in the positions specified.</p>
                        </div>
                    </div>
                    <p>After you use Module Builder to generate your modules, customize this page in each module for its intended purpose:</p>
                    <ol style="list-style-type: decimal;">
                        <li>Replace the placeholder text and heading with appropriate content for the module.</li>
                        <li>Create additional sections (with H3 headings) as necessary.</li>
                        <li>When you are finished editing the page, if you want to delete these instructions, click the green action items icon at the lower right ( <i class="dp-icon fas fa-tasks cp-bg-success dp-i-shape-rounded" aria-hidden="true"> </i>) to open the action items window. Then click the&nbsp;<strong>Remove&nbsp;</strong>button under the instruction block and close the action items window.</li>
                    </ol>
                </li>
            </ol>
        </div>
        </#if>
        <p>Enter_the_introduction_here</p>
        <h3>Section heading (H3)</h3>
        <p>Add_the_content_for_the_first_section_here.</p>
        <p>&nbsp;</p>
    </div>
</div>
