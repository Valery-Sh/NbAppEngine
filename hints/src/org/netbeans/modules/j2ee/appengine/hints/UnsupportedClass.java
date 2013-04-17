/**
 *  This file is part of Google App Engine suppport in NetBeans IDE.
 *
 *  Google App Engine suppport in NetBeans IDE is free software: you can
 *  redistribute it and/or modify it under the terms of the GNU General
 *  Public License as published by the Free Software Foundation, either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  Google App Engine suppport in NetBeans IDE is distributed in the hope
 *  that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Google App Engine suppport in NetBeans IDE.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.j2ee.appengine.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.j2ee.appengine.hints.Bundle.*;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

// XXX should this use the Whitelist API instead? currently only exported to friends...

/**
 *
 * @author Jan Lahoda
 */
// XXX how to set localized display name on category? formerly: org-netbeans-modules-java-hints/rules/hints/appengine=Google App Engine
@Hint(category="appengine", displayName="#DN_UnsupportedClass", description="#DESC_UnsupportedClass", severity=Severity.WARNING)
@Messages({
    "DN_UnsupportedClass=Unsupported Class",
    "DESC_UnsupportedClass=Class not supported in Google App Engine"
})
public class UnsupportedClass {

    private static final Logger LOG = Logger.getLogger(UnsupportedClass.class.getName());
    
    private static final String APPENGINE_SERVER_ID = "AppEngine";

    private static final Set<String> jreWhitelist;

    static {
        Set<String> whitelist = new HashSet<String>();
        URL url = UnsupportedClass.class.getResource("class-white-list");
        FileObject file = URLMapper.findFileObject(url);
        
        try {
            for (String line : file.asLines("UTF-8")) {
                whitelist.add(line);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        jreWhitelist = whitelist;
    }

    private static final Map<CompilationInfo, Boolean> hasAppEngine = new WeakHashMap<CompilationInfo, Boolean>();

    private static boolean hasAppEngine(CompilationInfo info) {
        Boolean val = hasAppEngine.get(info);

        if (val != null) {
            return val;
        }
        
        Project p = FileOwnerQuery.getOwner(info.getFileObject());

        if (p == null) {
            hasAppEngine.put(info, false);
            return false;
        }

        J2eeModuleProvider provider = p.getLookup().lookup(J2eeModuleProvider.class);

        if (provider == null) {
            hasAppEngine.put(info, false);
            return false;
        }

        String serverID = provider.getServerID();

        if (!APPENGINE_SERVER_ID.equals(serverID)) {
            hasAppEngine.put(info, false);
            return false;
        }

        hasAppEngine.put(info, true);
        return true;
    }

    private static final ClassPath EMPTY = ClassPathSupport.createClassPath(new FileObject[0]);
    
    private static final Map<CompilationInfo, Set<String>> info2ProjectWhitelist = new WeakHashMap<CompilationInfo, Set<String>>();

    private static Set<String> getProjectBasedWhitelist(CompilationInfo info) {
        Set<String> cached = info2ProjectWhitelist.get(info);

        if (cached != null) {
            return cached;
        }

        long start = System.currentTimeMillis();
        
        ClasspathInfo cpInfo = ClasspathInfo.create(EMPTY, info.getClasspathInfo().getClassPath(PathKind.COMPILE), info.getClasspathInfo().getClassPath(PathKind.SOURCE));
        Set<String> result = new HashSet<String>();
        Set<ElementHandle<TypeElement>> declaredTypes = cpInfo.getClassIndex().getDeclaredTypes("", NameKind.PREFIX, EnumSet.of(SearchScope.DEPENDENCIES, SearchScope.SOURCE));

        if (declaredTypes == null) {
            return null;
        }
        
        for (ElementHandle<TypeElement> h : declaredTypes) {
            result.add(h.getBinaryName());
        }

        long end = System.currentTimeMillis();

        Logger.getLogger("TIMER").log(Level.FINE, "Project Based Whitelist", new Object[] {info.getFileObject(), (end - start)});

        info2ProjectWhitelist.put(info, result);
        
        return result;
    }
    
    @Messages({"# {0} - class name", "ERR_UnsupportedClass=Class {0} not supported by the Google App Engine"})
    @TriggerTreeKind({Kind.IDENTIFIER, Kind.MEMBER_SELECT})
    public static List<ErrorDescription> run(HintContext context) {
        CompilationInfo info = context.getInfo();
        TreePath treePath = context.getPath();
        if (!hasAppEngine(info) || isUnitTest(info)) {
            return Collections.emptyList();
        }

        Element el = info.getTrees().getElement(treePath);

        if (el == null || (!el.getKind().isClass() && !el.getKind().isInterface())) {
            return Collections.emptyList();
        }

        TypeMirror tm = info.getTrees().getTypeMirror(treePath);

        if (tm == null || tm.getKind() == TypeKind.ERROR) {
            return Collections.emptyList();
        }

        TypeElement te = (TypeElement) el;
        String fqn = ElementHandle.create(te).getBinaryName();

        if (jreWhitelist.contains(fqn)) {
            return Collections.emptyList();
        }
        
        Set<String> projectBasedWhitelist = getProjectBasedWhitelist(info);

        if (projectBasedWhitelist == null) {
            return Collections.emptyList();
        }

        if (projectBasedWhitelist.contains(fqn)) {
            return Collections.emptyList();
        }
        
        return Collections.singletonList(forName(info, treePath.getLeaf(), ERR_UnsupportedClass(fqn)));
    }

    private static ErrorDescription forName(CompilationInfo info, Tree tree, String text, Fix... fixes) {
        int[] span = computeNameSpan(tree, info);

        if (span != null && span[0] != (-1) && span[1] != (-1)) {
            return org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(Severity.WARNING, text, Arrays.asList(fixes), info.getFileObject(), span[0], span[1]);
        }

        return null;
    }

    private static int[] computeNameSpan(Tree tree, CompilationInfo info) {
        switch (tree.getKind()) {
            case METHOD:
                return info.getTreeUtilities().findNameSpan((MethodTree) tree);
            case CLASS:
                return info.getTreeUtilities().findNameSpan((ClassTree) tree);
            case VARIABLE:
                return info.getTreeUtilities().findNameSpan((VariableTree) tree);
            case MEMBER_SELECT:
                return info.getTreeUtilities().findNameSpan((MemberSelectTree) tree);
            case METHOD_INVOCATION:
                return computeNameSpan(((MethodInvocationTree) tree).getMethodSelect(), info);
            default:
                return new int[] {
                    (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree),
                    (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree),
                };
        }
    }

    private static boolean isUnitTest(CompilationInfo info) {
        ClassPath sourcePath = info.getClasspathInfo().getClassPath(PathKind.SOURCE);

        if (sourcePath == null) {
            LOG.log(Level.FINE, "No source path for {0}", FileUtil.getFileDisplayName(info.getFileObject()));

            return true;
        }
        
        FileObject ownerRoot = sourcePath.findOwnerRoot(info.getFileObject());

        if (ownerRoot == null) {
            LOG.log(Level.FINE, "No owning root for {0} on its own source path", FileUtil.getFileDisplayName(info.getFileObject()));

            return true;
        }
        
        return UnitTestForSourceQuery.findSources(ownerRoot).length > 0;
    }
}