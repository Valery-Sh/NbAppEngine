/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.j2ee.appengine.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.appengine.AppEngineDeploymentManager;
import org.netbeans.modules.j2ee.appengine.util.Utils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.execution.ExecutorTask;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Project",
        id = "org.netbeans.modules.j2ee.appengine.maven.RunDevServer"
)
@ActionRegistration(
        displayName = "#CTL_RunDevServer"
)
@ActionReference(path = "Projects/Actions", position = 0)

@Messages("CTL_RunDevServer=Run dev GAE Server")
public final class RunDevServer extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(RunDevServer.class.getName());
    public static Process process;
    public static ExecutorTask serverTask;

    /**
     * Never called.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    /**
     * Creates an action for the given context. The created action implements
     * the {@code Presenter.Popup} interface.
     *
     * @param context a lookup that contains the server project instance of type
     * {@code Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new RunDevServer.ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private Lookup context;

        public ContextAction(Lookup context) {

            this.context = context;
            final Project project = context.lookup(Project.class);
            //String name = ProjectUtils.getInformation(project).getDisplayName();
            putValue(NAME, "&Run dev Server");

        }

        /*        public @Override
         void actionPerformed(ActionEvent e) {
         Utils.out("MAVEN:   ACTION PERFORMED");
         final Project project = context.lookup(Project.class);
         //   ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
         //   ap.invokeAction(ActionProvider.COMMAND_REBUILD, context);
         List<String> goals = new ArrayList<>();
         goals.add("package");
         RunConfig rc = RunUtils.createRunConfig(new File(project.getProjectDirectory().getPath()), project, "PACKAGING MY", goals);
         final ExecutorTask task = RunUtils.run(rc);
            
         final AppEngineDeploymentManager dm = AppEnginePluginUtils.getDeploymentManager(project);
         ///////////

         Project currentSelected = dm.getSelected();

         if (!dm.isServerRunning()) {
         //13.06          dm.setServerNeedsRestart(true);
         dm.setSelected(project);
         //                return;
         }
         //
         // server is allready running
         //
         if (!project.equals(currentSelected)) {
         dm.setSelected(project);
         //                return;
         }
         //
         // server is allready running and same selected
         //
         AppEngineServerMode currentMode = dm.getServerMode();
         AppEngineServerMode newMode = AppEngineServerMode.NORMAL;


         //task.waitFinished();
         AppEngineStartServer.getInstance(dm).startDeploymentManager();

         ///////////            
         Utils.out("BEFORE runAntTargetMaven");
         */
        /*            RequestProcessor rp = new RequestProcessor();
         RequestProcessor.Task rptask = rp.post(new Runnable() {

         @Override
         public void run() {
         //            AppEngineMavenUtils.runAntTarget(project, "runserver", new Properties());
         }
         });
         */
            //process = AppEngineMavenUtils.runAntTargetMaven(project, "runserver", new Properties());
        //serverTask = AppEngineMavenUtils.runAntTarget(project, "runserver", new Properties());            
/*            Utils.out("AFTER runAntTargetMaven");
         //            ActionUtils.runTarget(null, targetNames, null)
         */
//        }
        public @Override
        void actionPerformed(ActionEvent e) {
            Utils.out("MAVEN:   ACTION PERFORMED");
            final Project project = context.lookup(Project.class);
            ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
            ap.invokeAction(ActionProvider.COMMAND_DEBUG, context);
        }
        /*            RequestProcessor rp = new RequestProcessor();
         RequestProcessor.Task rptask = rp.post(new Runnable() {

         @Override
         public void run() {
         //            AppEngineMavenUtils.runAntTarget(project, "runserver", new Properties());
         }
         });
         */
            //process = AppEngineMavenUtils.runAntTargetMaven(project, "runserver", new Properties());
        //serverTask = AppEngineMavenUtils.runAntTarget(project, "runserver", new Properties());            
/*            Utils.out("AFTER runAntTargetMaven");
         //            ActionUtils.runTarget(null, targetNames, null)
         */
//        }
        public boolean isDebuggable(AppEngineDeploymentManager dm) {
            String s = null;
            // It's not in debug mode
            if (!dm.isServerRunning() || null == dm.getServerMode() || dm.getServerMode() == Deployment.Mode.RUN || dm.getServerMode() == Deployment.Mode.PROFILE) {
                return false;
            }
            // It's in debug mode
            return true;
        }

    }//class

}
