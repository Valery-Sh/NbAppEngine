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
package org.netbeans.modules.j2ee.appengine.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jindrich Sedek
 */
class AppEngineDataModel {

    private static final String EMPTY_STRING = "";
    private static final String EXCLUDE = "exclude";
    private static final String INCLUDE = "include";
    private static final String PATH = "path";
    private static final String WEBAPPENGINE = "appengine-web-app";
    private static final int ROOT_TAG_SEQUENCE_COUNT = 8;
    private static final String[] ROOT_TAG_SEQUENCE = new String[]{
        "application", "version",
        "static-files", "resource-files",
        "system-properties", "env-variables",
        "ssl-enabled", "sessions-enabled"
    };
    private ChangeListener changeListener;
    private Document root;
    private String appName;
    private String appVersion;
    private Files staticFiles;
    private Files resourceFiles;
    private Map<String, String> systemProperties;
    private Map<String, String> envVariables;
    private Boolean sslEnabled = false;
    private Boolean sessionEnabled = false;

    private AppEngineDataModel(Document root) {
        this.root = root;
        initialize();
    }

    private Node getAppengineRootNode(){
        NodeList rootNodes = getRoot().getChildNodes();
        for(int i = 0; i< rootNodes.getLength(); i++){
            Node rootNode = rootNodes.item(i);
            if (WEBAPPENGINE.equals(rootNode.getNodeName()) && Node.ELEMENT_NODE == rootNode.getNodeType()){
                return rootNode;
            }
        }
        Node newRoot = getRoot().createElement(WEBAPPENGINE);
        newRoot = getRoot().appendChild(newRoot);
        return newRoot;
    }

    private void initialize() {
        Node webAppEngineNode = getAppengineRootNode();
        NodeList childList = webAppEngineNode.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (ROOT_TAG_SEQUENCE[0].equalsIgnoreCase(child.getNodeName())) {
                appName = child.getTextContent();
            } else if (ROOT_TAG_SEQUENCE[1].equalsIgnoreCase(child.getNodeName())) {
                appVersion = child.getTextContent();
            } else if (ROOT_TAG_SEQUENCE[2].equalsIgnoreCase(child.getNodeName())) {
                staticFiles = parseFiles(child);
            } else if (ROOT_TAG_SEQUENCE[3].equalsIgnoreCase(child.getNodeName())) {
                resourceFiles = parseFiles(child);
            } else if (ROOT_TAG_SEQUENCE[4].equalsIgnoreCase(child.getNodeName())) {
                systemProperties = parseMap(child, "property");
            } else if (ROOT_TAG_SEQUENCE[5].equalsIgnoreCase(child.getNodeName())) {
                envVariables = parseMap(child, "env-var");
            } else if (ROOT_TAG_SEQUENCE[6].equalsIgnoreCase(child.getNodeName())) {
                sslEnabled = Boolean.parseBoolean(child.getTextContent());
            } else if (ROOT_TAG_SEQUENCE[7].equalsIgnoreCase(child.getNodeName())) {
                sessionEnabled = Boolean.parseBoolean(child.getTextContent());
            }
        }
        if (staticFiles == null) {
            staticFiles = new Files();
        }
        if (resourceFiles == null) {
            resourceFiles = new Files();
        }
        if (systemProperties == null) {
            systemProperties = new HashMap<String, String>();
        }
        if (envVariables == null) {
            envVariables = new HashMap<String, String>();
        }
    }

    private Map<String, String> parseMap(Node mapNode, String tagName) {
        Map<String, String> values = new HashMap<String, String>();
        NodeList childList = mapNode.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (tagName.equalsIgnoreCase(child.getNodeName())) {
                String name = getAttribute(child, "name");
                String value = getAttribute(child, "value");
                values.put(name, value);
            }
        }
        return values;
    }

    private Files parseFiles(Node filesNode) {
        List<String> includes = new ArrayList<String>();
        List<String> excludes = new ArrayList<String>();
        NodeList childList = filesNode.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (INCLUDE.equalsIgnoreCase(child.getNodeName())) {
                includes.add(getPathAttribute(child));
            } else if (EXCLUDE.equalsIgnoreCase(child.getNodeName())) {
                excludes.add(getPathAttribute(child));
            }
        }
        return new Files(includes, excludes);
    }

    private String getAttribute(Node node, String attributeName) {
        NamedNodeMap attributes = node.getAttributes();
        return attributes.getNamedItem(attributeName).getTextContent();
    }

    private String getPathAttribute(Node node) {
        return getAttribute(node, PATH);
    }

    private void applyChanges() {
        Node webAppEngineNode = getAppengineRootNode();
        NodeList childList = webAppEngineNode.getChildNodes();
        int sequenceIndex = 0;
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            String nodeName = child.getNodeName();
            int foundIndex = -1;
            for (int j = sequenceIndex; j < ROOT_TAG_SEQUENCE_COUNT; j++) {
                if (ROOT_TAG_SEQUENCE[j].equals(nodeName)) {
                    foundIndex = j;
                }
            }

            if (foundIndex != -1) {
                for (int j = sequenceIndex; j < foundIndex; j++) {
                    createMissingNodeInRoot(webAppEngineNode, child, j);
                }
                switch (foundIndex) {
                    case 0:
                        child.setTextContent(appName);
                        break;
                    case 1:
                        child.setTextContent(appVersion);
                        break;
                    case 2:
                        applyFiles(child, staticFiles);
                        break;
                    case 3:
                        applyFiles(child, resourceFiles);
                        break;
                    case 4:
                        applyProperties(child);
                        break;
                    case 5:
                        applyVariables(child);
                        break;
                    case 6:
                        child.setTextContent(sslEnabled.toString());
                        break;
                    case 7:
                        child.setTextContent(sessionEnabled.toString());
                        break;
                }
                sequenceIndex = foundIndex + 1;
            }
        }
        for (int j = sequenceIndex; j < ROOT_TAG_SEQUENCE_COUNT; j++) {
            createMissingNodeInRoot(webAppEngineNode, null, j);
        }
    }

    private void applyFiles(Node parent, Files files) {
        Document doc = parent.getOwnerDocument();
        List<String> includes = files.getNonEmptyIncludes();
        List<Node> toRemove = new ArrayList<Node>();
        List<String> excludes = files.getNonEmptyExcludes();
        NodeList children = parent.getChildNodes();
        int i = 0;
        while (i < children.getLength()) {
            Node child = children.item(i);
            if (INCLUDE.equals(child.getNodeName())) {
                String path = getPathAttribute(child);
                if (includes.contains(path)) {
                    includes.remove(path);
                } else {
                    toRemove.add(child);
                }
            } else if (EXCLUDE.equals(child.getNodeName())) {
                break;
            }
            i++;
        }
        if (!includes.isEmpty()) {
            for (String path : includes) {
                Node newChild = doc.createElement(INCLUDE);
                Node pathAttribute = doc.createAttribute(PATH);
                pathAttribute.setNodeValue(path);
                newChild.getAttributes().setNamedItem(pathAttribute);
                parent.insertBefore(newChild, children.item(i));
            }
        }
        while (i < children.getLength()) {
            Node child = children.item(i);
            if (EXCLUDE.equals(child.getNodeName())) {
                String path = getPathAttribute(child);
                if (excludes.contains(path)) {
                    excludes.remove(path);
                } else {
                    toRemove.add(child);
                }
            }
            i++;
        }
        if (!excludes.isEmpty()) {
            for (String path : excludes) {
                Node newChild = doc.createElement(EXCLUDE);
                Node pathAttribute = doc.createAttribute(PATH);
                pathAttribute.setNodeValue(path);
                newChild.getAttributes().setNamedItem(pathAttribute);
                parent.appendChild(newChild);
            }
        }
        for (Node node : toRemove) {
            parent.removeChild(node);
        }
    }

    private void applyProperties(Node parent) {
        applyPropertiesImpl(parent, systemProperties, "property");
    }

    private void applyVariables(Node parent) {
        applyPropertiesImpl(parent, envVariables, "env-var");
    }

    private void applyPropertiesImpl(Node parent, Map<String, String> properties, String tagName) {
        List<Node> toRemove = new ArrayList<Node>();
        NodeList childNodes = parent.getChildNodes();
        //modify existing nodes
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (tagName.equals(n.getNodeName())) {
                String name = getAttribute(n, "name");
                String value = getAttribute(n, "value");
                String newValue = properties.get(name);
                if (newValue == null) {
                    toRemove.add(n);
                } else if (!newValue.equals(value)) {
                    n.getAttributes().getNamedItem("value").setTextContent(newValue);
                }
            }
        }
        //remove removed nodes
        for (Node node : toRemove) {
            parent.removeChild(node);
        }
        //add missing nodes
        Document doc = parent.getOwnerDocument();
        Set<String> toAdd = new HashSet<String>(properties.keySet());
        Map<String, String> existing = parseMap(parent, tagName);
        toAdd.removeAll(existing.keySet());
        for (String propertyName : toAdd) {
            Node newNode = doc.createElement(tagName);
            Node newName = doc.createAttribute("name");
            newName.setTextContent(propertyName);
            newNode.getAttributes().setNamedItem(newName);

            Node newValue = doc.createAttribute("value");
            newValue.setTextContent(properties.get(propertyName));
            newNode.getAttributes().setNamedItem(newValue);

            parent.appendChild(newNode);
        }
    }

    private void createMissingNodeInRoot(Node parentNode, Node beforeNode, int j) {
        Node newChild = parentNode.getOwnerDocument().createElement(ROOT_TAG_SEQUENCE[j]);
        switch (j) {
            case 0:
                newChild.setTextContent(appName);
                break;
            case 1:
                newChild.setTextContent(appVersion);
                break;
            case 2:
                if (staticFiles.isEmpty()) {
                    return;
                }
                applyFiles(newChild, staticFiles);
                break;
            case 3:
                if (resourceFiles.isEmpty()) {
                    return;
                }
                applyFiles(newChild, resourceFiles);
                break;
            case 4:
                if (systemProperties.isEmpty()) {
                    return;
                }
                applyProperties(newChild);
                break;
            case 5:
                if (envVariables.isEmpty()) {
                    return;
                }
                applyVariables(newChild);
                break;
            case 6:
                if (!sslEnabled) {
                    return;
                }
                newChild.setTextContent(sslEnabled.toString());
                break;
            case 7:
                if (!sessionEnabled) {
                    return;
                }
                newChild.setTextContent(sessionEnabled.toString());
                break;
        }
        if (beforeNode == null) {
            parentNode.appendChild(newChild);
        } else {
            parentNode.insertBefore(newChild, beforeNode);
        }
    }

    private void applyAndNotifyChanges() {
        applyChanges();
        notifyChanges();
    }

    public static AppEngineDataModel createModel(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        Document doc = XMLUtil.parse(new InputSource(is), false, false, null, EntityCatalog.getDefault());
        return new AppEngineDataModel(doc);
    }

    /**
     * @return the applicationName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @param applicationName the applicationName to set
     */
    public void setAppName(String appName) {
        if (!this.appName.equals(appName)) {
            this.appName = appName;
            applyAndNotifyChanges();
        }
    }

    /**
     * @return the appVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @param appVersion the appVersion to set
     */
    public void setAppVersion(String appVersion) {
        if (!this.appVersion.equals(appVersion)) {
            this.appVersion = appVersion;
            applyAndNotifyChanges();
        }
    }

    /**
     * @return the root
     */
    Document getRoot() {
        return root;
    }

    void reload(InputStream is) throws IOException, SAXException {
        root = XMLUtil.parse(new InputSource(is), false, false, null, EntityCatalog.getDefault());
        initialize();
    }

    /**
     * @return the staticFiles
     */
    public Files getStaticFiles() {
        return staticFiles;
    }

    /**
     * @return the resourceFiles
     */
    public Files getResourceFiles() {
        return resourceFiles;
    }

    /**
     * @return the systemProperties
     */
    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    /**
     * @return the envVariables
     */
    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    /**
     * @return the sslEnabled
     */
    public Boolean getSslEnabled() {
        return sslEnabled;
    }

    /**
     * @param sslEnabled the sslEnabled to set
     */
    public void setSslEnabled(Boolean sslEnabled) {
        if (!this.sslEnabled.equals(sslEnabled)) {
            this.sslEnabled = sslEnabled;
            applyAndNotifyChanges();
        }
    }

    /**
     * @return the sessionEnabled
     */
    public Boolean getSessionEnabled() {
        return sessionEnabled;
    }

    /**
     * @param sessionEnabled the sessionEnabled to set
     */
    public void setSessionEnabled(Boolean sessionEnabled) {
        if (!this.sessionEnabled.equals(sessionEnabled)) {
            this.sessionEnabled = sessionEnabled;
            applyAndNotifyChanges();
        }
    }

    private void verifyEmptyLines() {
        if (!hasEmptyLine(resourceFiles.includes) || !hasEmptyLine(resourceFiles.excludes)) {
            resourceFiles.includes.add(EMPTY_STRING);
            resourceFiles.excludes.add(EMPTY_STRING);
        }
        if (!hasEmptyLine(staticFiles.includes) || !hasEmptyLine(staticFiles.excludes)) {
            staticFiles.includes.add(EMPTY_STRING);
            staticFiles.excludes.add(EMPTY_STRING);
        }
    }

    private boolean hasEmptyLine(List<String> includes) {
        if (includes.isEmpty()) {
            return false;
        }
        return includes.get(includes.size() - 1).length() == 0;// last line is empty
    }

    public static class Files {

        final List<String> includes;
        final List<String> excludes;

        public Files(List<String> includes, List<String> excludes) {
            this.includes = includes;
            this.excludes = excludes;
            int lenght = Math.max(includes.size(), excludes.size()) + 1;
            while (includes.size() < lenght) {
                includes.add(EMPTY_STRING);
            }
            while (excludes.size() < lenght) {
                excludes.add(EMPTY_STRING);
            }
        }

        private Files() {
            this(new ArrayList<String>(), new ArrayList<String>());
        }

        private List<String> getNonEmpty(List<String> items) {
            List<String> result = new ArrayList<String>();
            for (String item : items) {
                if (item.length() > 0) {
                    result.add(item);
                }
            }
            return result;
        }

        private List<String> getNonEmptyIncludes() {
            return getNonEmpty(includes);
        }

        private List<String> getNonEmptyExcludes() {
            return getNonEmpty(excludes);
        }

        private boolean isEmpty() {
            int wholeSize = getNonEmptyIncludes().size() + getNonEmptyExcludes().size();
            return wholeSize == 0;
        }
    }

    public void setChangeListener(ChangeListener chl) {
        changeListener = chl;
    }

    private void notifyChanges() {
        if (changeListener != null) {
            changeListener.stateChanged(null);
        }
    }

    void notifyChangedFilesOrProperties() {
        verifyEmptyLines();
        applyAndNotifyChanges();
    }
}
