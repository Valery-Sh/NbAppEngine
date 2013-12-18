package org.netbeans.modules.j2ee.appengine.deployment;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * The class permits the invoker of an action to monitor its progress.
 * 
 *
 * @author V. Shyshkin
 */
public class CommandActionProgress extends ActionProgress implements Runnable{
    private static final Logger LOG = Logger.getLogger(CommandActionProgress.class.getName());

    private boolean finished;
    private boolean failed;
    private boolean checked;
    
    private Lookup jointContext;
    private Project project;
    
    protected CommandActionProgress() {
    }

    private static CommandActionProgress getInstance(Lookup actionContext) {
        CommandActionProgress cap = new CommandActionProgress();
        cap.jointContext = new ProxyLookup(actionContext, Lookups.fixed(cap));
        
        return cap;
    }

    public boolean isChecked() {
        
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    
    /**
     * Creates a new instance of the class for the specified project.
     * 
     * The project may be of any type: {@literal j2se, web} etc. But the {@literal plugin}
     * uses this class for web projects.
     * 
     * @param project 
     * @return new instance of the class
     */
    public static CommandActionProgress getInstance(Project project) {
        CommandActionProgress cap = getInstance(project.getLookup());
        //cap.jointContext = new ProxyLookup(actionContext, Lookups.fixed(cap));
        cap.project = project;
        return cap;
    }
    /**
     * Called when the action is started. Serves no purpose other 
     * than confirming to the caller that this action provider does 
     * in fact support this interface and that it should wait for 
     * action completion. If this method is not called, the caller 
     * will not know when the action is truly finished. 
     * Called automatically by {@literal start(org.openide.util.Lookup) }, so 
     * action providers need not pay attention. 
     */
    @Override
    protected void started() {
        finished = false;
        failed = false;
        DeployUtils.out("STARTED PROJECT " + project.getProjectDirectory().getNameExt() + "; TIME=" + (new Date()));
        
    }
    /**
     * Called when the action has completed.
     * The method is called by NetBeans API.
     * Set the value of property {@link #finished} to {@literal true}.
     * Set the value of property {@link #failed} to the value of the expression:
     * <pre>
     * {@literal !success}. 
     * </pre>
     * @param success if {@literal true} then the method 
     * {@literal isFailed()} will return {@literal false}. if {@literal false} then the method 
     * {@literal isFailed()} will return {@literal true}. 
     */
    @Override
    public void finished(boolean success) {
        finished = true;
        if (!success) {
            failed = true;
        }
    }
    /**
     * Indicates whether the action has completed.
     * @return {@literal true} if the action has completed. {@literal false} otherwise
     */
    public boolean isFinished() {
        return finished;
    }
    /**
     * Provides the result of the action.
     * 
     * @return {@literal true} if the action has completed with errors.
     * {@literal false} otherwise
     */
    public boolean isFailed() {
        return failed;
    }
    /**
     * Returns an object of type {@literal Lookup} of the
     * instance of the class.
     * 
     * @return a {@literal Lookup} object which contains an actionContext
     *  passed as a parameter to the {@link #getInstance(org.netbeans.api.project.Project)
     *  method call and an instance of this class.
     */
    public Lookup getContext() {
        return jointContext;
    }
    /**
     * Returns the project passed as a parameter to 
     * {@literal getInstance(org.netbeans.api.project.Project)}
     * method call.
     * 
     * @return an instance of the {@literal Project}
     */
    public Project getProject() {
        return project;
    }
    /**
     * Invokes the action specified by the second parameter 
     * for the project specified by the first parameter.
     * The method invokes the actions and returns immediately. 
     * @param project
     * @param actionCommand a command as specified by the ActionProveder
     *  constants.
     * 
     * @return an instance of this class 
     */
    public static CommandActionProgress invokeAction(final Project project, final String actionCommand) {

        final CommandActionProgress listener = CommandActionProgress.getInstance(project);
        ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
//        ActionProgress.start(listener.getContext());
        
        provider.invokeAction(actionCommand, listener.getContext());
        
        return listener;
    }
    /**
     * Invokes the action specified by the second parameter 
     * for the project specified by the first parameter.
     * The method invokes the actions waits finished. 
     * @param project
     * @param actionCommand a command as specified by the ActionProveder
     *  constants.
     * 
     * @return {@literal true} if the action has completed successfully.
     *  {@literal false} otherwise
     */
    public static boolean invokeActionAndWait(final Project project, final String actionCommand) {

        final CommandActionProgress listener = CommandActionProgress.getInstance(project);
        ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
        ActionProgress.start(listener.getContext());
        provider.invokeAction(actionCommand, listener.getContext());
        
        while (!listener.isFinished()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
DeployUtils.out("invokeActionAndWait return time=" + new Date());
        return ! listener.isFailed();
    }
    
    @Override
    public void run() {
        while (! isFinished()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
            }
        }
        
    }
}
