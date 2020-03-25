<style>
#azure-plugin-status .status-report-page-header {
  top:             40px;
  width:           100%;
  height:          50px;
  display:         flex;
  z-index:         10;
  position:        fixed;
  background:      #fff;
  box-shadow:      0 0 0 #000;
  border-bottom:   1px solid #ddd;
  background-size: cover;
}

#azure-plugin-status .status-report-page-header .page_title {
  font-size: 17px;
  font-weight: 600;
  margin: 0;
  padding: 0 30px;
  align-self: center;
}

#azure-plugin-status .status-report-page-header .plugin-version-info {
  position: absolute;
  padding: 0 30px;
  align-self: center;
  font-size: 12px;
  right: 0;
  color: #555;
}

#azure-plugin-status .status-report-page-header .plugin-version-info label {
  color: #555;
  display: inline-block;
  font-size: 12px;
  font-weight: bold;
}

#azure-plugin-status .aggregates {
  margin: 0 30px 20px 30px;
  display: flex;
  justify-content: left;
}

#azure-plugin-status .aggregates ul {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
}

#azure-plugin-status .aggregates ul li {
  width: 32%;
  padding: 0 10px 0 0;
}

#azure-plugin-status label.key {
  display: inline-block;
  line-height: 25px;
}

#azure-plugin-status .aggregates ul li .key {
  font-weight: 400;
  font-size: 15px;
  min-width: 40%;
}

#azure-plugin-status .aggregates ul li .value {
  font-weight: 600;
  font-size: 15px;
  padding-left: 10px;
}

#azure-plugin-status span.value {
  padding-left: 10px;
  display: inline-block;
  line-height: 25px;
}

#azure-plugin-status span.value:before {
  content: ":";
  margin-right: 10px;
}

#azure-plugin-status .agent-report {
  margin: 0 30px 20px 30px;
  background: white;
}

#azure-plugin-status .agent-report.expanded {
  box-shadow: 0 3px 11px 0 rgba(0, 0, 0, 0.15);
}

#azure-plugin-status .icon {
  font-family:            'FontAwesome';
  font-weight:            normal;
  font-style:             normal;
  text-decoration:        inherit;
  color:                  #7e5959
  position:               absolute;
  font-size:              25px;
  -webkit-font-smoothing: antialiased;
  float:                  right;
  width:                  20px;
}

#azure-plugin-status .icon.collapsed:before {
  content: "\f105";
}

#azure-plugin-status .icon.expanded:before {
  content: "\f107";
}


#azure-plugin-status .agent-report .header {
  padding: 5px 10px;
  background: #d1c4e9;
  border-radius: 3px 3px 3px 3px;
  display: block;
}

#azure-plugin-status .agent-report.expanded .header {
  border-radius: 3px 3px 0 0 ;
}

#azure-plugin-status .agent-report .header .name {
  display: inline-block;
  font-size: 15px;
}

#azure-plugin-status .agent-report .header .platform {
  float: right;
  width: 25%;
}

#azure-plugin-status .agent-report .header .state {
  float: right;
  width: 25%;
}

#azure-plugin-status .agent-report .header .key {
  font-weight: 600;
  font-size: 15px;
}

#azure-plugin-status .agent-report .header .value {
  font-weight: 500;
  font-size: 13px;
}

#azure-plugin-status .agent-report .properties {
  display: flex;
  justify-content: left;
  padding: 10px 10px 15px 10px;
}


#azure-plugin-status .agent-report .properties ul li .key {
  font-weight: 500;
  font-size: 13px;
  display: inline-block;
  width: 25%;
}

#azure-plugin-status .agent-report .properties ul li .value {
  font-weight: 600;
  font-size: 12px;
  padding-left: 10px;
  display: inline-block;
  max-width: 72%;
  vertical-align: top;
}

#azure-plugin-status .agent-report .properties ul {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
}

#azure-plugin-status .agent-report .properties ul li.parameter {
  width: 30%;
  border-bottom: 1px dashed #e6e6e6;
  vertical-align: top;
  padding-top: 5px;
  margin-right: 40px;
}

</style>

<div id="azure-plugin-status">
  <div class="status-report-page-header">
    <h1 class="page_title">Azure plugin status report</h1>
      <div class="plugin-version-info">
        <label>Version: </label>
        <span>${version !}</span>
      </div>
  </div>
  <div class="aggregates">
    <ul>
      <#list properties as property>
        <li><label class="key">${property.key}</label><span class="value">${property.value}</span></li>
      </#list>
    </ul>
  </div>

  <#list agentStatusReports as agentStatus>
    <#assign index = agentStatus?index>
    <#assign ngModel = "AgentStatus${index}">
     <div class="agent-report" ng-init="${ngModel} = true" ng-class="{'expanded' : ${ngModel}}">
       <div class="header" ng-click="${ngModel} = !${ngModel}">
         <div class="name"><label class="key">Name</label><span class="value">${agentStatus.elasticAgentId !}</span></div>
         <i class="icon expanded" aria-hidden="true" ng-show="${ngModel}"></i>
         <i class="icon collapsed" aria-hidden="true" ng-hide="${ngModel}"></i>
         <div class="platform"><label class="key">Platform</label><span class="value">${agentStatus.platform?capitalize !}</span></div>
         <div class="state"><label class="key">State</label><span class="value">${agentStatus.state !}</span></div>
       </div>
       <div class="properties" ng-model="${ngModel}" ng-value="false" ng-show="${ngModel}">
         <ul>
           <li class="parameter">
             <label class="key">Image</label><span class="value">${agentStatus.image !}</span>
           </li>
           <li class="parameter">
             <label class="key">OS</label><span class="value">${agentStatus.os !}</span>
           </li>
           <li class="parameter">
             <label class="key">Size</label><span class="value">${agentStatus.size !}</span>
           </li>
           <li class="parameter">
             <label class="key">OS disk size</label><span class="value">${agentStatus.diskSize !}</span>
           </li>
           <li class="parameter">
             <label class="key">Status</label><span class="value">${agentStatus.status !}</span>
           </li>
           <li class="parameter">
             <label class="key">Created at</label><span class="value">${agentStatus.createdAt !}</span>
           </li>
           <li class="parameter">
             <label class="key">Resource group</label><span class="value">${agentStatus.resourceGroup !}</span>
           </li>
         </ul>
       </div>
     </div>
  </#list>
</div>