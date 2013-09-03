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

import java.io.File;
import java.util.*;

import org.apache.maven.model.Model;
import org.eclipse.aether.ant.types.Pom;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;

/**
 * Workspace reader caching available POMs and artifacts for ant builds.
 * <p/>
 * &lt;pom> elements are cached if they are defined by the 'file'-attribute, as they reference a backing pom.xml file that
 * can be used for resolution with Aether. &lt;artifact> elements are cached if they directly define a 'pom'-attribute
 * or child. The POM may be file-based or in-memory.
 */
public class ProjectWorkspaceReader
    implements WorkspaceReader
{

    private static ProjectWorkspaceReader instance;

    private static Object lock = new Object();

    private Map<String, Artifact> artifacts = Collections.synchronizedMap( new HashMap<String, Artifact>() );

    public void addPom( Pom pom )
    {
        if ( pom.getFile() != null )
        {
            Model model = pom.getModel( pom );
            Artifact aetherArtifact = new DefaultArtifact( model.getGroupId(), model.getArtifactId(), null,
                                                           "pom", model.getVersion(), null, pom.getFile()  );
            artifacts.put( ArtifactIdUtils.toId( aetherArtifact ), aetherArtifact );
        }
    }

    public void addArtifact( org.eclipse.aether.ant.types.Artifact artifact )
    {
        if ( artifact.getPom() != null )
        {
            Pom pom = artifact.getPom();
            Artifact aetherArtifact;
            if ( pom.getFile() != null )
            {
                Model model = pom.getModel( pom );
                aetherArtifact =
                    new DefaultArtifact( model.getGroupId(), model.getArtifactId(), artifact.getClassifier(),
                                         artifact.getType(), model.getVersion(), null, artifact.getFile() );
            }
            else
            {
                aetherArtifact =
                    new DefaultArtifact( pom.getGroupId(), pom.getArtifactId(), artifact.getClassifier(),
                                         artifact.getType(), pom.getVersion(), null, artifact.getFile() );
            }
            artifacts.put( ArtifactIdUtils.toId( aetherArtifact ), aetherArtifact );
        }
    }

    public WorkspaceRepository getRepository()
    {
        return new WorkspaceRepository( "ant" );
    }

    public File findArtifact( Artifact artifact )
    {
        Artifact projectArtifact = artifacts.get( ArtifactIdUtils.toId( artifact ) );
        return projectArtifact == null ? null : projectArtifact.getFile();
    }

    public List<String> findVersions( Artifact artifact )
    {
        List<String> versions = new ArrayList<String>();
        String versionlessId = ArtifactIdUtils.toVersionlessId( artifact );
        for ( Artifact projectArtifact : artifacts.values() )
        {
            if ( versionlessId.equals( ArtifactIdUtils.toVersionlessId( projectArtifact )  ) )
            {
                versions.add( projectArtifact.getVersion() );
            }
        }
        return versions;
    }

    ProjectWorkspaceReader()
    {
    }

    public static ProjectWorkspaceReader getInstance()
    {
        if ( instance != null )
        {
            return instance;
        }

        synchronized ( lock )
        {
            if ( instance == null )
            {
                instance = new ProjectWorkspaceReader();
            }
            return instance;
        }
    }

    public static void dropInstance()
    {
        instance = null;
    }
}
