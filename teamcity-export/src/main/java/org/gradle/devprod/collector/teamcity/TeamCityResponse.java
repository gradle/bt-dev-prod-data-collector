package org.gradle.devprod.collector.teamcity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/*
  This class is created with IDEA "GsonFormatPlus" plugin.

  {
  "count": 100,
  "nextHref": "/app/rest/builds?affectedProject%3A%28id%3AGradle_Check%29%2CfailedToStart%3Afalse%2Cstatus%3AFAILURE%2Cbranch%3Aall%2CsinceDate%3A20211031T153557%2B0000%2CuntilDate%3A20211101T153557%2B0000%2Ccount=100&fields=nextHref%2Ccount%2Cbuild%28id%2Cagent%28name%29%2CbuildType%28id%2Cname%2CprojectName%29%2CfailedToStart%2Crevisions%28revision%28version%2CvcsRoot%28vcsRootId%29%29%29%2CbranchName%2Cstatus%2CstatusText%2Cstate%2CqueuedDate%2CstartDate%2CfinishDate%29&locator=count:100,start:100",
  "build": [
    {
      "id": 46968030,
      "status": "SUCCESS",
      "state": "finished",
      "failedToStart": false,
      "branchName": "master",
      "statusText": "Success",
      "buildType": {
        "id": "Enterprise_Master_Operations_GradleEnterpriseEnvironments_ShipUnstableCheckForUpdates",
        "name": "Ship Unstable - Check For Updates",
        "projectName": "Enterprise / Master / Operation / Gradle Enterprise Environments"
      },
      "queuedDate": "20211101T103657+0100",
      "startDate": "20211101T103658+0100",
      "finishDate": "20211101T103723+0100",
      "revisions": {
        "revision": [
          {
            "version": "400ec00125f6222d2b338a3fc184e9b048ca3c09"
          }
        ]
      },
      "agent": {
        "name": "dev96-agent1"
      }
    }
  ]
}
 */

public class TeamCityResponse {
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("nextHref")
    private String nextHref;
    @JsonProperty("build")
    private List<BuildBean> build;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNextHref() {
        return nextHref;
    }

    public void setNextHref(String nextHref) {
        this.nextHref = nextHref;
    }

    public List<BuildBean> getBuild() {
        return build;
    }

    public void setBuild(List<BuildBean> build) {
        this.build = build;
    }

    public static class BuildBean {
        @JsonProperty("id")
        private Integer id;
        @JsonProperty("status")
        private String status;
        @JsonProperty("state")
        private String state;
        @JsonProperty("failedToStart")
        private Boolean failedToStart;
        @JsonProperty("branchName")
        private String branchName;
        @JsonProperty("statusText")
        private String statusText;
        @JsonProperty("buildType")
        private BuildTypeBean buildType;
        @JsonProperty("queuedDate")
        private String queuedDate;
        @JsonProperty("startDate")
        private String startDate;
        @JsonProperty("finishDate")
        private String finishDate;
        @JsonProperty("revisions")
        private RevisionsBean revisions;
        @JsonProperty("agent")
        private AgentBean agent;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Boolean getFailedToStart() {
            return failedToStart;
        }

        public void setFailedToStart(Boolean failedToStart) {
            this.failedToStart = failedToStart;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public BuildTypeBean getBuildType() {
            return buildType;
        }

        public void setBuildType(BuildTypeBean buildType) {
            this.buildType = buildType;
        }

        public String getQueuedDate() {
            return queuedDate;
        }

        public void setQueuedDate(String queuedDate) {
            this.queuedDate = queuedDate;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getFinishDate() {
            return finishDate;
        }

        public void setFinishDate(String finishDate) {
            this.finishDate = finishDate;
        }

        public RevisionsBean getRevisions() {
            return revisions;
        }

        public void setRevisions(RevisionsBean revisions) {
            this.revisions = revisions;
        }

        public AgentBean getAgent() {
            return agent;
        }

        public void setAgent(AgentBean agent) {
            this.agent = agent;
        }

        public static class BuildTypeBean {
            @JsonProperty("id")
            private String id;
            @JsonProperty("name")
            private String name;
            @JsonProperty("projectName")
            private String projectName;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getProjectName() {
                return projectName;
            }

            public void setProjectName(String projectName) {
                this.projectName = projectName;
            }
        }

        public static class RevisionsBean {
            @JsonProperty("revision")
            private List<RevisionBean> revision;

            public List<RevisionBean> getRevision() {
                return revision;
            }

            public void setRevision(List<RevisionBean> revision) {
                this.revision = revision;
            }

            public static class RevisionBean {
                @JsonProperty("version")
                private String version;

                public String getVersion() {
                    return version;
                }

                public void setVersion(String version) {
                    this.version = version;
                }
            }
        }

        public static class AgentBean {
            @JsonProperty("name")
            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
