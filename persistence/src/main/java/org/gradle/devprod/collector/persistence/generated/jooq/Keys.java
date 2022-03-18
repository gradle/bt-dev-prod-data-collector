/*
 * This file is generated by jOOQ.
 */
package org.gradle.devprod.collector.persistence.generated.jooq;


import org.gradle.devprod.collector.persistence.generated.jooq.tables.Build;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.LongTest;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.TeamcityBuild;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.TeamcityBuildQueueLength;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.BuildRecord;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.LongTestRecord;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.TeamcityBuildQueueLengthRecord;
import org.gradle.devprod.collector.persistence.generated.jooq.tables.records.TeamcityBuildRecord;
import org.jooq.ForeignKey;
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
    public static final UniqueKey<LongTestRecord> LONG_TEST_PK = Internal.createUniqueKey(LongTest.LONG_TEST, DSL.name("long_test_pk"), new TableField[] { LongTest.LONG_TEST.BUILD_ID, LongTest.LONG_TEST.CLASS_NAME }, true);
    public static final UniqueKey<TeamcityBuildRecord> TEAMCITY_BUILD_PK = Internal.createUniqueKey(TeamcityBuild.TEAMCITY_BUILD, DSL.name("teamcity_build_pk"), new TableField[] { TeamcityBuild.TEAMCITY_BUILD.BUILD_ID }, true);
    public static final UniqueKey<TeamcityBuildQueueLengthRecord> TEAMCITY_BUILD_QUEUE_LENGTH_PKEY = Internal.createUniqueKey(TeamcityBuildQueueLength.TEAMCITY_BUILD_QUEUE_LENGTH, DSL.name("teamcity_build_queue_length_pkey"), new TableField[] { TeamcityBuildQueueLength.TEAMCITY_BUILD_QUEUE_LENGTH.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<LongTestRecord, BuildRecord> LONG_TEST__LONG_TEST_BUILD_FK = Internal.createForeignKey(LongTest.LONG_TEST, DSL.name("long_test_build_fk"), new TableField[] { LongTest.LONG_TEST.BUILD_ID }, Keys.BUILD_PK, new TableField[] { Build.BUILD.BUILD_ID }, true);
}
