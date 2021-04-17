/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq;


import org.gradle.devprod.collector.persistence.generated.jooq.tables.Build;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.TeamcityBuild;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.BuildRecord;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.TeamcityBuildRecord;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<BuildRecord> BUILD_PK = Internal.createUniqueKey(Build.BUILD, DSL.name("build_pk"), new TableField[] { Build.BUILD.BUILD_ID }, true);
    public static final UniqueKey<TeamcityBuildRecord> TEAMCITY_BUILD_PK = Internal.createUniqueKey(TeamcityBuild.TEAMCITY_BUILD, DSL.name("teamcity_build_pk"), new TableField[] { TeamcityBuild.TEAMCITY_BUILD.BUILD_ID }, true);
}
