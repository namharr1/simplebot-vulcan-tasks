package me.remie.vulcan.leaguetasks;

import me.remie.vulcan.leaguetasks.task.LeagueTask;
import me.remie.vulcan.leaguetasks.task.tasks.*;
import me.remie.vulcan.leaguetasks.task.tasks.agility.DraynorRooftopAgility;
import me.remie.vulcan.leaguetasks.task.tasks.agility.TreeGnomeAgility;
import me.remie.vulcan.leaguetasks.task.tasks.agility.VarrockRooftopAgility;
import me.remie.vulcan.leaguetasks.task.tasks.emotes.EmoteExplore;
import me.remie.vulcan.leaguetasks.task.tasks.emotes.EmoteUriTransformation;
import me.remie.vulcan.leaguetasks.task.tasks.slayer.CheckSlayerTask;
import me.remie.vulcan.leaguetasks.task.tasks.slayer.SlayerTaskDuradel;
import me.remie.vulcan.leaguetasks.task.tasks.thieving.ThievingLevel20;
import me.remie.vulcan.leaguetasks.task.tasks.thieving.ThievingStealSilk;
import net.runelite.api.ChatMessageType;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import simple.robot.utils.ScriptUtils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Reminisce on Mar 04, 2024 at 08:44 PM
 *
 * @author Reminisce <thereminisc3@gmail.com>
 * @Discord reminisce <138751815847116800>
 */
@ScriptManifest(author = "Reminisce", name = "RLeagues Tasks", category = Category.OTHER,
        version = "0.0.1", description = "Completes a ton of leagues tasks", discord = "reminisce", servers = {"Vulcan"}, vip = true)
public class LeagueScript extends Script implements LoopingScript, MouseListener {

    private long startTime;
    private String status;

    private boolean checkedTasks;
    private LeagueTask currentTask;
    private int pointsGained, pointsTotal;
    private List<LeagueTask> tasks;

    private LeagueScriptPaint paintHelper;

    @Override
    public void onExecute() {
        this.startTime = System.currentTimeMillis();
        this.status = "Waiting to start...";
        this.tasks = Arrays.asList(
                new OpenLeaguesMenu(this),
                new EmoteExplore(this),
                new EmoteUriTransformation(this),
                new EquipMithrilGloves(this),
                new EquipElementalStaff(this),
                new SlayerTaskDuradel(this),
                new CheckSlayerTask(this),
                new ThievingLevel20(this),
                new ThievingStealSilk(this),
                new EnterDeathsDomain(this),
                new TravelSpiritTrees(this),
                new TreeGnomeAgility(this),
                new DraynorRooftopAgility(this),
                new VarrockRooftopAgility(this)
        );
        setupPaint();
    }

    private void setupPaint() {
        if (this.paintHelper != null) {
            return;
        }
        this.paintHelper = new LeagueScriptPaint(this);
        paintHelper.addLine(() -> "Points: " + ctx.paint.formatValue(pointsGained));
    }

    @Override
    public void onProcess() {
        if (!checkedTasks) {
            checkTasks();
            return;
        }
        if (this.currentTask == null || this.currentTask.isCompleted()) {
            getNewTask();
            return;
        }
        this.currentTask.run();
    }

    private void checkTasks() {
        final SimpleWidget leaguesWidget = ctx.widgets.getWidget(657, 18);
        if (leaguesWidget == null || leaguesWidget.isHidden()) {
            ctx.log("Opening leagues menu");
            ctx.game.tab(Game.Tab.QUESTS);
            ctx.menuActions.clickButton(41222167);
            ctx.sleep(1000);
            ctx.menuActions.clickButton(42991640);
            ctx.onCondition(() -> {
                final SimpleWidget leaguesWidget1 = ctx.widgets.getWidget(657, 18);
                return leaguesWidget1 != null && !leaguesWidget1.isHidden();
            }, 1000, 5);
            return;
        }
        final SimpleWidget[] children = leaguesWidget.getDynamicChildren();
        if (children == null || children.length == 0) {
            return;
        }
        for (SimpleWidget child : children) {
            final String taskName = ScriptUtils.stripHtml(child.getText().toLowerCase());
            this.tasks.stream().filter(task -> task.getName().equalsIgnoreCase(taskName)).findFirst().ifPresent((task) -> {
                if (child.getWidget().getTextColor() == 0xff7700) {
                    task.setCompleted(true);
                    ctx.log("Task " + task.getName() + " is already completed");
                }
            });
        }
        ctx.sleep(1500, 2500);
        ctx.menuActions.clickButton(43057155);
        ctx.onCondition(() -> {
            final SimpleWidget leaguesWidget1 = ctx.widgets.getWidget(657, 18);
            return leaguesWidget1 == null || leaguesWidget1.isHidden();
        }, 1000, 5);
        this.checkedTasks = true;
    }

    private void getNewTask() {
        if (this.currentTask != null) {
            this.currentTask.setCompleted(true);
        }
        Optional<LeagueTask> newTask = tasks.stream().filter(task -> !task.isCompleted() && task.canComplete()).findFirst();
        if (newTask.isPresent()) {
            this.currentTask = newTask.get();
        }
    }

    public boolean isTaskCompleted(Class<? extends LeagueTask> task) {
        for (LeagueTask t : this.tasks) {
            if (t.getClass().equals(task)) {
                return t.isCompleted();
            }
        }
        return false;
    }

    public <T extends LeagueTask> T getTask(Class<T> task) {
        for (LeagueTask t : this.tasks) {
            if (t.getClass().equals(task)) {
                return (T) t;
            }
        }
        return null;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public String getScriptStatus() {
        return this.status;
    }

    @Override
    public void onTerminate() {
        ctx.log("Gained " + this.pointsGained + " points, total: " + this.pointsTotal);
    }

    @Override
    public void onChatMessage(ChatMessage e) {
        //ChatMessage(messageNode=co@334973e2, type=GAMEMESSAGE, name=, message=Congratulations, you've completed an elite leagues task:<col=a34c00> Use the Explore Emote, sender=null, timestamp=1709605487)
        //ChatMessage(messageNode=co@39d519bf, type=GAMEMESSAGE, name=, message=You have earned <col=a34c00>200</col> league points. You now have <col=a34c00>220</col> league points., sender=null, timestamp=1709605487)
        if (e.getType() != ChatMessageType.GAMEMESSAGE || e.getSender() != null || !e.getName().isEmpty()) {
            return;
        }
        final String message = ScriptUtils.stripHtml(e.getMessage().toLowerCase());
        if ((message.startsWith("congratulations, you've completed an ") || message.startsWith("congratulations, you've completed a ")) && message.contains("leagues task:")) {
            final String taskName = message.split(":")[1].trim();
            this.tasks.stream().filter(task -> task.getName().equalsIgnoreCase(taskName)).findFirst().ifPresent((task) -> task.setCompleted(true));
        } else if (message.startsWith("you have earned") && message.contains("league points. you now have")) {
            //You have earned 200 league points. You now have 220 league points.
            Pattern pattern = Pattern.compile("you have earned (\\d+) league points\\. you now have (\\d+) league points\\.");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                final int pointsEarned = Integer.parseInt(matcher.group(1));
                this.pointsGained += pointsEarned;
                final int totalPoints = Integer.parseInt(matcher.group(2));
                this.pointsTotal = totalPoints;
                ctx.log("Gained " + pointsEarned + " points, total: " + totalPoints);
            }
        }
    }

    @Override
    public void paint(Graphics g1) {
        if (this.paintHelper == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g1;
        this.paintHelper.drawPaint(g);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (this.paintHelper != null) {
            this.paintHelper.handleMouseClick(e);
        }
    }

    @Override
    public int loopDuration() {
        return 150;
    }

    @Override
    public void mousePressed(final MouseEvent e) {

    }

    @Override
    public void mouseReleased(final MouseEvent e) {

    }

    @Override
    public void mouseEntered(final MouseEvent e) {

    }

    @Override
    public void mouseExited(final MouseEvent e) {

    }
}