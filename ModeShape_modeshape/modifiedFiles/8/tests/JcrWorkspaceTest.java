/*
 * ModeShape (http://www.modeshape.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors. 
 *
 * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * ModeShape is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.modeshape.jcr;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import org.junit.Before;
import org.junit.Test;
import org.modeshape.common.FixFor;

/**
 * @author jverhaeg
 */
public class JcrWorkspaceTest extends SingleUseAbstractTest {

    private JcrWorkspace workspace;
    private String workspaceName;

    private JcrSession otherSession;
    private JcrWorkspace otherWorkspace;
    private String otherWorkspaceName;

    @Override
    @Before
    public void beforeEach() throws Exception {
        super.beforeEach();

        Node root = session.getRootNode();
        Node a = root.addNode("a");
        Node ab = a.addNode("b", "nt:unstructured");
        Node abc = ab.addNode("c");
        Node b = root.addNode("b");
        abc.setProperty("stringProperty", "value");
        session.save();
        assertThat(b, is(notNullValue()));

        workspace = session.getWorkspace();
        workspaceName = workspace.getName();

        otherWorkspaceName = "anotherWs";
        workspace.createWorkspace(otherWorkspaceName);
        otherSession = repository.login(otherWorkspaceName);
        otherWorkspace = otherSession.getWorkspace();
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowCloneWithNullWorkspaceName() throws Exception {
        workspace.clone(null, "/src", "/dest", false);
    }

    @Test
    @FixFor( "MODE-1972" )
    public void shouldCloneEntireWorkspaces() throws Exception {
        otherWorkspace.clone(workspaceName, "/", "/", true);

        assertEquals(session.getNode("/a").getIdentifier(), otherSession.getNode("/a").getIdentifier());
        assertEquals(session.getNode("/a/b").getIdentifier(), otherSession.getNode("/a/b").getIdentifier());
        assertEquals(session.getNode("/a/b/c").getIdentifier(), otherSession.getNode("/a/b/c").getIdentifier());
        assertEquals(session.getNode("/b").getIdentifier(), otherSession.getNode("/b").getIdentifier());
    }

    @Test( expected = RepositoryException.class )
    @FixFor( "MODE-1972" )
    public void shouldNotClonePartialWorkspaceIntoWorkspaceRoot() throws Exception {
        otherWorkspace.clone(workspaceName, "/a/b", "/", false);
    }

    @Test
    @FixFor( "MODE-2007" )
    public void shouldCloneChildrenOfRoot() throws Exception {
        otherWorkspace.clone(workspaceName, "/a", "/a", false);
        otherWorkspace.clone(workspaceName, "/b", "/b", false);

        assertEquals(session.getNode("/a").getIdentifier(), otherSession.getNode("/a").getIdentifier());
        assertEquals(session.getNode("/a/b").getIdentifier(), otherSession.getNode("/a/b").getIdentifier());
        assertEquals(session.getNode("/a/b/c").getIdentifier(), otherSession.getNode("/a/b/c").getIdentifier());
        assertEquals(session.getNode("/b").getIdentifier(), otherSession.getNode("/b").getIdentifier());
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowCopyFromNullPathToNullPath() throws Exception {
        workspace.copy(null, null);
    }

    @Test
    public void shouldCopyFromPathToAnotherPathInSameWorkspace() throws Exception {
        workspace.copy("/a/b", "/b/b-copy");
    }

    @Test
    @FixFor( "MODE-1972" )
    public void shouldCopyEntireWorkspaces() throws Exception {
        otherWorkspace.copy(workspaceName, "/", "/");

        assertNotNull(otherSession.getNode("/a"));
        assertNotNull(otherSession.getNode("/a/b"));
        assertNotNull(otherSession.getNode("/a/b/c"));
        assertNotNull(otherSession.getNode("/b"));
    }

    @Test( expected = RepositoryException.class )
    @FixFor( "MODE-1972" )
    public void shouldNotCopyPartialWorkspaceIntoWorkspaceRoot() throws Exception {
        otherWorkspace.copy(workspaceName, "/a/b", "/");
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowCopyFromOtherWorkspaceWithNullWorkspace() throws Exception {
        workspace.copy(null, null, null);
    }

    @Test
    public void shouldAllowGetAccessibleWorkspaceNames() throws Exception {
        List<String> names = Arrays.asList(workspace.getAccessibleWorkspaceNames());
        assertThat(names.size(), is(2));
        assertThat(names.contains(workspaceName), is(true));
        assertThat(names.contains(otherWorkspaceName), is(true));
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowImportContentHandlerWithNullPath() throws Exception {
        workspace.getImportContentHandler(null, 0);
    }

    @Test
    public void shouldGetImportContentHandlerWithValidPath() throws Exception {
        assertThat(workspace.getImportContentHandler("/b", 0), is(notNullValue()));
    }

    @Test
    public void shouldProvideName() throws Exception {
        assertThat(workspace.getName(), is(workspaceName));
    }

    @Test
    public void shouldHaveSameContextIdAsSession() {
        assertThat(workspace.context().getId(), is(session.context().getId()));
    }

    @Test
    public void shouldProvideNamespaceRegistry() throws Exception {
        NamespaceRegistry registry = workspace.getNamespaceRegistry();
        assertThat(registry, is(notNullValue()));
        assertThat(registry.getURI(JcrLexicon.Namespace.PREFIX), is(JcrLexicon.Namespace.URI));
    }

    @Test
    public void shouldGetNodeTypeManager() throws Exception {
        assertThat(workspace.getNodeTypeManager(), is(notNullValue()));
    }

    @Test
    public void shouldGetObservationManager() throws Exception {
        assertThat(workspace.getObservationManager(), is(notNullValue()));
    }

    @Test
    public void shouldProvideQueryManager() throws Exception {
        assertThat(workspace.getQueryManager(), notNullValue());
    }

    @Test
    public void shouldCreateQuery() throws Exception {
        String statement = "SELECT * FROM [nt:unstructured]";

        QueryManager queryManager = workspace.getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);

        assertThat(query, is(notNullValue()));
        assertThat(query.getLanguage(), is(Query.JCR_SQL2));
        assertThat(query.getStatement(), is(statement));
    }

    @Test
    public void shouldStoreQueryAsNode() throws Exception {
        String statement = "SELECT * FROM [nt:unstructured]";

        QueryManager queryManager = workspace.getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);

        Node node = query.storeAsNode("/storedQuery");
        assertThat(node, is(notNullValue()));
        assertThat(node.getPrimaryNodeType().getName(), is("nt:query"));
        assertThat(node.getProperty("jcr:language").getString(), is(Query.JCR_SQL2));
        assertThat(node.getProperty("jcr:statement").getString(), is(statement));
    }

    @Test
    public void shouldLoadStoredQuery() throws Exception {
        String statement = "SELECT * FROM [nt:unstructured]";

        QueryManager queryManager = workspace.getQueryManager();
        Query query = queryManager.createQuery(statement, Query.JCR_SQL2);

        Node node = query.storeAsNode("/storedQuery");

        Query loaded = queryManager.getQuery(node);

        assertThat(loaded, is(notNullValue()));
        assertThat(loaded.getLanguage(), is(Query.JCR_SQL2));
        assertThat(loaded.getStatement(), is(statement));
        assertThat(loaded.getStoredQueryPath(), is(node.getPath()));
    }

    @Test
    public void shouldProvideSession() throws Exception {
        assertThat(workspace.getSession(), is(notNullValue()));
    }

    @Test
    public void shouldAllowImportXml() throws Exception {
        String inputData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                           + "<sv:node xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" "
                           + "xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" sv:name=\"workspaceTestNode\">"
                           + "<sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\">"
                           + "<sv:value>nt:unstructured</sv:value></sv:property></sv:node>";
        workspace.importXML("/", new ByteArrayInputStream(inputData.getBytes()), 0);
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowMoveFromNullPath() throws Exception {
        workspace.move(null, null);
    }

    @Test
    public void shouldAllowMoveFromPathToAnotherPathInSameWorkspace() throws Exception {
        workspace.move("/a/b", "/b/b-copy");
    }

    protected void assertNotFound( String absPath,
                                   JcrSession jcrSession ) throws RepositoryException {
        try {
            jcrSession.getNode(absPath);
            fail("Node " + absPath + " should not have been found in the session " + session);
        } catch (PathNotFoundException e) {
            // expected
        }
    }
}
