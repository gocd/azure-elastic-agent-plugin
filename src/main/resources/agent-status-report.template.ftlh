<style>
  .main-container {
    margin-top: 0;
  }

  [data-plugin-style-id="azure-plugin"] .azure-elastic-agent {
    padding: 60px 20px 0 20px;
  }

  [data-plugin-style-id="azure-plugin"] .tabs {
    position: relative;
    clear:    both;
  }

  [data-plugin-style-id="azure-plugin"] .tabs .tab {
    float: left;
  }

  [data-plugin-style-id="azure-plugin"] .tabs .tab .content {
  }

  [data-plugin-style-id="azure-plugin"] .tab label {
    background-color: #d1c4e9;
    padding:          7px 15px;
    line-height:      19px;
    font-size:        13px;
    border-radius:    3px 3px 0 0;
    font-weight:      600;
    color:            #333;
  }

  [data-plugin-style-id="azure-plugin"] .tab-content {
    margin-top: 0;
    height:     calc(100vh - 280px);
    overflow-x: hidden;
  }

  [data-plugin-style-id="azure-plugin"] .tab-content-outer {
    background: white;
    padding:    20px;
  }

  [data-plugin-style-id="azure-plugin"] .warning-message {
    background-color: #fdf5e2;
    color:            #b88c34;
    font-weight:      600;
    font-size:        15px;
    padding:          10px 20px;
    border-radius:    3px;
    margin:           0 0 1rem 0;
    border:           1px solid rgba(10, 10, 10, 0.25);
    position:         relative;
  }

  [data-plugin-style-id="azure-plugin"] .entity_title span.label {
    position:   absolute;
    margin-top: -10px;
    font-size:  0.625em;
  }

  [data-plugin-style-id="azure-plugin"] .status-report-page-header {
    width:           100%;
    height:          50px;
    z-index:         10;
    position:        fixed;
    background:      #fff;
    box-shadow:      0 0 0 #000;
    border-bottom:   1px solid #ddd;
    background-size: cover;
  }

  [data-plugin-style-id="azure-plugin"] .status-report-page-header .entity_title {
    top:      12px;
    left:     20px;
    position: absolute;
  }

  [data-plugin-style-id="azure-plugin"] .status-report-page-header .entity_title .last h1 {
    margin-top: 2px !important;
  }

  [data-plugin-style-id="azure-plugin"] .status-report-page-header .entity_title li a {
    font-size:   0.9375rem;
    font-weight: 600;
    line-height: normal;
  }

  [data-plugin-style-id="azure-plugin"] textarea[readonly="readonly"], textarea[readonly] {
    cursor: auto;
  }

  [data-plugin-style-id="azure-plugin"] .name-value {
    float:           none;
    list-style-type: none;
  }

  [data-plugin-style-id="azure-plugin"] .name-value_pair {
    float:         none;
    display:       flex;
    border-bottom: 1px dotted #ddd;
  }

  [data-plugin-style-id="azure-plugin"] .name-value_pair label {
    width:       300px;
    padding:     5px 10px 5px 0px;
    min-width:   150px;
    word-wrap:   break-word;
    font-size:   13px;
    font-weight: 600;
  }

  [data-plugin-style-id="azure-plugin"] .name-value_pair span {
    padding:     5px 10px 5px 0px;
    font-size:   13px;
    line-height: 20px;
  }

  [data-plugin-style-id="azure-plugin"] table {
    text-align:      left;
    border-spacing:  0;
    border-collapse: collapse;
  }

  [data-plugin-style-id="azure-plugin"] table thead tr th {
    font-weight:   600;
    border-bottom: 2px solid #ddd;
  }

  [data-plugin-style-id="azure-plugin"] .name-value_pair table tr th,
  [data-plugin-style-id="azure-plugin"] .name-value_pair table tr td {
    padding:       8px 8px 8px 0px;
    border-bottom: 1px dotted #ddd;
  }

  [data-plugin-style-id="azure-plugin"] table tr th,
  [data-plugin-style-id="azure-plugin"] table tr td {
    padding:        8px;
    font-size:      13px;
    border-top:     0;
    line-height:    1.42857143;
    border-bottom:  1px solid #ddd;
    vertical-align: bottom;
  }

  [data-plugin-style-id="azure-plugin"] .logs {
    height: 90%;
    background-color: #333;
    padding: 20px;
    color: white;
    font-size: 14px;
    line-height: 18px;
    white-space: pre-wrap;
    overflow: auto;
  }


</style>

<div data-plugin-style-id="azure-plugin" class="azure-plugin">
  <div class="status-report-page-header">
    <ul class="entity_title">
      <li class="last">
        <span class="label">Elastic Agent Id</span>
        <h1>${ elasticAgentId !}</h1>
      </li>
    </ul>
  </div>

  <div class="content_wrapper_inner azure-elastic-agent">
    <div class="row">
      <div class="sub_tabs_container">
        <ul class="tabs" ng-init="currenttab=currentab || 'container-details'">
          <li ng-class="{'current_tab' : currenttab == 'container-details' }" ng-click="currenttab='container-details'">
            <a href="#">Details</a>
          </li>
          <#if customScriptExecutionLogs ?has_content>
            <li ng-class="{'current_tab' : currenttab == 'custom-script-logs' }" ng-click="currenttab='custom-script-logs'">
              <a href="#">Custom script execution logs</a>
            </li>
          </#if>
        </ul>
      </div>
      <div class="tab-content-outer">
        <div class="tab-content container-details" ng-show="currenttab == 'container-details'">
          <ul class="name-value">
            <li class="name-value_pair">
              <label>Hostname</label>
              <span>${ hostName !}</span>
            </li>

            <li class="name-value_pair">
              <label>Image</label>
              <span>${ image !}</span>
            </li>
            <li class="name-value_pair">
              <label>Size</label>
              <span>${ size !}</span>
            </li>
            <li class="name-value_pair">
              <label>Status</label>
              <span>${ status !}</span>
            </li>
            <li class="name-value_pair">
              <label>OS</label>
              <span>${ os !}</span>
            </li>
            <li class="name-value_pair">
              <label>OS Disksize</label>
              <span>${ diskSize !}</span>
            </li>
            <li class="name-value_pair">
              <label>Environment</label>
              <span>${ environment !}</span>
            </li>

            <li class="name-value_pair">

              <label>Created At</label>
              <span>${ createdAt !}</span>
            </li>
            <li class="name-value_pair">
              <label>Resource Group</label>
              <span>${ resourceGroup !}</span>
            </li>
            <li class="name-value_pair">
              <label>NIC</label>
              <span>${ nic !}</span>
            </li>

          </ul>
        </div>
        <#if customScriptExecutionLogs ?has_content>
          <div class="tab-content container-details" ng-show="currenttab == 'custom-script-logs'">
            <div class="logs">${ customScriptExecutionLogs !}</div>
          </div>
        </#if>
      </div>
    </div>
  </div>
</div>


