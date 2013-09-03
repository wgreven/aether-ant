/*******************************************************************************
 * Copyright (c) 2010, 2012 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.ant;

import org.apache.tools.ant.Project;
import org.eclipse.aether.ant.types.Pom;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.junit.Assert.*;
import static org.junit.internal.matchers.IsCollectionContaining.*;

public class ProjectWorkspaceReaderTest
{

    private ProjectWorkspaceReader reader;

    private Project project;

    @Before
    public void setUp()
        throws Exception
    {
        this.reader = new ProjectWorkspaceReader();

        this.project = new Project();
        project.setProperty( "user.home", System.getProperty( "user.home" ) );
    }

    private Artifact artifact( String coords )
    {
        return new DefaultArtifact( coords );
    }

    @Test
    public void testFindPom()
    {
        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );
        
        reader.addPom( pom );
        
        assertEquals( new File( "src/test/ant/dummy-pom.xml" ),
                      reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindNoPom()
    {
        assertNull( reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        assertNull( reader.findArtifact( artifact( "unavailable:test:pom:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindNoArtifact()
    {
        assertNull( reader.findArtifact( artifact( "test:test:jar:0.1-SNAPSHOT" ) ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        org.eclipse.aether.ant.types.Artifact artifact = new org.eclipse.aether.ant.types.Artifact();
        artifact.setProject( project );
        artifact.addPom( pom );
        artifact.setFile( new File( "src/test/ant/common.xml" ) );

        reader.addArtifact( artifact );

        assertNull( reader.findArtifact( artifact( "unavailable:test:jar:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindArtifact()
    {
        assertNull( reader.findArtifact( artifact( "test:test:jar:0.1-SNAPSHOT" ) ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        org.eclipse.aether.ant.types.Artifact artifact = new org.eclipse.aether.ant.types.Artifact();
        artifact.setProject( project );
        artifact.addPom( pom );
        artifact.setFile( new File( "src/test/ant/common.xml" ) );

        reader.addArtifact( artifact );

        assertNull( reader.findArtifact( artifact( "test:test:jar:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindVersions()
    {
        assertThat( reader.findVersions( artifact( "test:test:pom:0.1-SNAPSHOT" ) ),
                    hasSize( 0 ) );

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        assertThat( reader.findVersions( artifact( "test:test:pom:0.1-SNAPSHOT" ) ),
                    hasItems( "0.1-SNAPSHOT" ) );
    }

    @Test
    public void testFindVersionsWithMultipleVersions()
    {
        assertThat( reader.findVersions( artifact( "test:test:pom:0.1-SNAPSHOT" ) ),
                    hasSize(0));

        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( new File( "src/test/ant/dummy-pom.xml" ) );

        reader.addPom( pom );

        Pom pom2 = new Pom();
        pom2.setProject( project );
        pom2.setFile( new File( "src/test/ant/dummy-pom-0.2-SNAPSHOT.xml" ) );

        reader.addPom( pom2 );

        assertThat( reader.findVersions( artifact( "test:test:pom:0.1-SNAPSHOT" ) ),
                    hasItems( "0.1-SNAPSHOT", "0.2-SNAPSHOT" ) );
    }
}