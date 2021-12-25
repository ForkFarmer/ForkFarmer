package util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * @author liyifeng
 * @version 1.0
 * @date 2021/12/21 3:23 下午
 */
public class I18n {
    // user custom ForkFarmer.properties
    private static ResourceBundle userCustomResourceBundle = getCustomI18nResourceBundle();
    // i18n_xx_xx.properties by locale
    private static ResourceBundle systemLocaleResourceBundle = getBundleByLocale(Locale.getDefault());
    // system default i18n_en.properties
    private static ResourceBundle defaultEnglishResourceBundle = getBundleByLocale(Locale.ENGLISH);

    private static ResourceBundle getCustomI18nResourceBundle() {
        ResourceBundle bundle = null;
        BufferedInputStream inputStream = null;
        try {
            String proFilePath = System.getProperty("user.dir") + File.separator + "ForkFarmer.properties";
            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
            bundle = new PropertyResourceBundle(inputStream);
        } catch (Exception e) {

        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return bundle;
    }

    private static ResourceBundle getBundleByLocale(Locale locale) {
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle("i18n", locale);
        } catch (Exception e) {
        }
        return bundle;
    }

    /**
     * get the localized language word by key,and the key must has default value in i18n_en.properties
     * @param key - The key must not be null
     * @return
     */
    public static String get(String key) {
        if (key == null) {
            throw new RuntimeException("localized language key must not be null");
        }

        String val = getStringFromResourceBundle(key, userCustomResourceBundle);

        if (val == null)
            val = getStringFromResourceBundle(key, systemLocaleResourceBundle);

        if (val == null)
            val = getStringFromResourceBundle(key, defaultEnglishResourceBundle);

        // the key should has a default value in i18n_en.properties
        if (val == null) {
            throw new RuntimeException(key + " is not config in i18n_en.properties");
        }
        return val;
    }

    private static String getStringFromResourceBundle(String key, ResourceBundle resourceBundle) {
        if(resourceBundle != null && resourceBundle.containsKey(key)){
            return resourceBundle.getString(key);
        }
        return null;
    }

    public static String getWithVariable(String key, String... values) {
        String val = get(key);
        return replaceVar(val, values);
    }

    /**
     *  replace variable {1}、 {2} ... {n}
     */
    public static String replaceVar(String val, String... values) {
        if (val == null) {
            return null;
        }
        if (values != null) {
            int index = 1;
            for (String v : values) {
                val = val.replaceAll("\\{" + index + "\\}", v);
                index++;
            }
        }
        return val;
    }

    public static class ForkFarmer {
        public static final String forkFarmerTitle = I18n.get("ForkFarmer.title");
    }

    public static class ReadTime {
        public static final String readTimeout = I18n.get("ReadTime.timeout.text");
    }

    public static class MainGui {
        public static final String farmSize = I18n.get("MainGui.farmSize");
        public static final String value = I18n.get("MainGui.value");
        public static final String forks = I18n.get("MainGui.forks");
        public static final String createTransaction = I18n.get("MainGui.tx.createTransaction");
        public static final String sendBtn = I18n.get("MainGui.tx.send.btn");
        public static final String errorTitle = I18n.get("MainGui.tx.send.error.title");
        public static final String errorContent = I18n.get("MainGui.tx.send.error.content");

        public static final String settingTitle = I18n.get("MainGui.setting.title");
        public static final String logReader = I18n.get("MainGui.setting.logReader");
        public static final String daemonReader = I18n.get("MainGui.setting.daemonReader");
        public static final String currency = I18n.get("MainGui.setting.currency");
        public static final String guiHttpServer = I18n.get("MainGui.setting.guiHttpServer");
        public static final String automaticPriceOrColdWalletUpdates = I18n.get("MainGui.setting.automaticPriceOrColdWalletUpdates");
        public static final String intraDelay = I18n.get("MainGui.setting.intraDelay");
        public static final String exoDelay = I18n.get("MainGui.setting.exoDelay");
        public static final String workerThreads = I18n.get("MainGui.setting.workerThreads");
        public static final String delay = I18n.get("MainGui.setting.delay");
        public static final String symbol = I18n.get("MainGui.setting.symbol");
        public static final String ratio = I18n.get("MainGui.setting.ratio");
        public static final String xchforksUrl = I18n.get("MainGui.setting.xchforksUrl");
        public static final String lockColumns = I18n.get("MainGui.setting.lockColumns");
        public static final String enable = I18n.get("MainGui.setting.enable");
        public static final String port = I18n.get("MainGui.setting.port");
    }

    public static class ForkView {
        public static final String symbolColName = I18n.get("ForkView.tableHeader.symbolColName");
        public static final String nameColName = I18n.get("ForkView.tableHeader.nameColName");
        public static final String balanceColName = I18n.get("ForkView.tableHeader.balanceColName");
        public static final String equityColName = I18n.get("ForkView.tableHeader.equityColName");
        public static final String netspaceColName = I18n.get("ForkView.tableHeader.netspaceColName");
        public static final String heightColName = I18n.get("ForkView.tableHeader.heightColName");
        public static final String farmSizeColName = I18n.get("ForkView.tableHeader.farmSizeColName");
        public static final String versionColName = I18n.get("ForkView.tableHeader.versionColName");
        public static final String latestVerColName = I18n.get("ForkView.tableHeader.latestVerColName");
        public static final String publishedColName = I18n.get("ForkView.tableHeader.publishedColName");
        public static final String syncColName = I18n.get("ForkView.tableHeader.syncColName");
        public static final String farmColName = I18n.get("ForkView.tableHeader.farmColName");
        public static final String etwColName = I18n.get("ForkView.tableHeader.etwColName");
        public static final String h24hWinColName = I18n.get("ForkView.tableHeader.24h_winColName");
        public static final String h24hBwColName = I18n.get("ForkView.tableHeader.24h_bwColName");
        public static final String lastWinColName = I18n.get("ForkView.tableHeader.last_winColName");
        public static final String effortColName = I18n.get("ForkView.tableHeader.effortColName");
        public static final String addressColName = I18n.get("ForkView.tableHeader.addressColName");
        public static final String rewardColName = I18n.get("ForkView.tableHeader.rewardColName");
        public static final String walletsColName = I18n.get("ForkView.tableHeader.walletsColName");
        public static final String harvestersColName = I18n.get("ForkView.tableHeader.harvestersColName");
        public static final String loadColName = I18n.get("ForkView.tableHeader.loadColName");
        public static final String timeColName = I18n.get("ForkView.tableHeader.timeColName");
        public static final String fullNodeColName = I18n.get("ForkView.tableHeader.full_nodeColName");
        public static final String walletNodeColName = I18n.get("ForkView.tableHeader.wallet_nodeColName");
        public static final String atbStatusColName = I18n.get("ForkView.tableHeader.atb_statusColName");
    }

    public static class TransactionView {
        public static final String dateColName = I18n.get("TransactionView.tableHeader.dateColName");
        public static final String symbolColName = I18n.get("TransactionView.tableHeader.symbolColName");
        public static final String nameColName = I18n.get("TransactionView.tableHeader.nameColName");
        public static final String effortColName = I18n.get("TransactionView.tableHeader.effortColName");
        public static final String lastWinTimeColName = I18n.get("TransactionView.tableHeader.lastWinTimeColName");
        public static final String targetColName = I18n.get("TransactionView.tableHeader.targetColName");
        public static final String amountColName = I18n.get("TransactionView.tableHeader.amountColName");

        public static final String transactionTitle = I18n.get("TransactionView.table.title");
        public static final String viewAtATB = I18n.get("TransactionView.table.menu.viewAtATB");
        public static final String copy = I18n.get("TransactionView.table.menu.copy");
        public static final String report = I18n.get("TransactionView.table.menu.report");
        public static final String updateReward = I18n.get("TransactionView.table.menu.updateReward");
        public static final String title = I18n.get("TransactionView.title");

    }

    public static class TxReportView {
        public static final String value = I18n.get("TxReportView.table.value");
        public static final String averageEffort = I18n.get("TxReportView.table.averageEffort");
        public static final String txSymbolColName = I18n.get("TxReportView.tableHeader.symbolColName");
        public static final String txNameColName = I18n.get("TxReportView.tableHeader.nameColName");
        public static final String txAmountColName = I18n.get("TxReportView.tableHeader.amountColName");
    }

    public static class PeerView {

        public static final String getAtbBtn = I18n.get("PeerView.getAtbBtn");
        public static final String addPeerBtn = I18n.get("PeerView.addPeerBtn");
        public static final String copy = I18n.get("PeerView.copy");
        public static final String cliCopy = I18n.get("PeerView.cliCopy");
        public static final String addressColName = I18n.get("PeerView.tableHeader.addressColName");
        public static final String heightColName = I18n.get("PeerView.tableHeader.heightColName");
        public static final String timeColName = I18n.get("PeerView.tableHeader.timeColName");
        public static final String uploadColName = I18n.get("PeerView.tableHeader.uploadColName");
        public static final String dowloadColName = I18n.get("PeerView.tableHeader.dowloadColName");
        public static final String addPeerBtnTipText = I18n.get("PeerView.addPeerBtnTipText");
    }

    public static class LogModel {
        public static final String timeColName = I18n.get("LogModel.tableHeader.timeColName");
        public static final String descriptionColName = I18n.get("LogModel.tableHeader.descriptionColName");
    }


    public static class DebugView {
        public static final String startText = I18n.get("DebugView.start.text");
        public static final String noneExceptionText = I18n.get("DebugView.noneExceptionText");
        public static final String walletShowStartText = I18n.get("DebugView.walletShowText");
        public static final String exePath = I18n.get("DebugView.exePath");

        public static final String noneWalletNodeTip(String name) {
            return I18n.getWithVariable("DebugView.noneWalletNodeTip", name);
        }

        public static final String runningFarmSummary = I18n.get("DebugView.runningFarmSummary");
        public static final String debugFinish = I18n.get("DebugView.debugFinish");
    }

    public static class ForkLogViewer {
        public static final String title = I18n.get("ForkLogViewer.title");
        public static final String autoUpdate = I18n.get("ForkLogViewer.autoUpdate");
        public static final String lines2Read = I18n.get("ForkLogViewer.lines2Read");
        public static final String singleForkTitleSuffix = I18n.get("ForkLogViewer.singleForkTitleSuffix");
        public static final String multiForkTabNameSuffix = I18n.get("ForkLogViewer.multiForkTabNameSuffix");
    }

    public static class Fork {
        public static final String showDebugFrameTitle = I18n.get("Fork.DebugView.title");
        public static final String sendTransactionMsgTitle = I18n.get("Fork.sendTransaction.msgTitle");
    }

    public static class ManualAddView {
        public static final String configPath = I18n.get("ManualAddView.configPath");
        public static final String exePath = I18n.get("ManualAddView.exePath");
        public static final String logPath = I18n.get("ManualAddView.logPath");
        public static final String walletNode = I18n.get("ManualAddView.walletNode");
        public static final String fullNode = I18n.get("ManualAddView.fullNode");
        public static final String afterLoadTip = I18n.get("ManualAddView.afterLoadTip");

    }

    public static class PlotCalc {
        public static final String hddSize = I18n.get("PlotCalc.hddSize");
        public static final String borderPlotSize = I18n.get("PlotCalc.border.plotSize");
        public static final String tableHeaderKColName = I18n.get("PlotCalc.tableHeader.kColName");
        public static final String tableHeaderPlotSizeColName = I18n.get("PlotCalc.tableHeader.plotSizeColName");
        public static final String solutionColName = I18n.get("PlotCalc.solution.tableHeader.kColName");
        public static final String freeSpaceColName = I18n.get("PlotCalc.solution.tableHeader.freeSpaceColName");

    }

    public static class MissingForks {
        public static final String viewAllCheckBoxLabel = I18n.get("MissingForks.viewAllCheckBoxLabel");
        public static final String symbolColName = I18n.get("MissingForks.tableHeader.symbolColName");
        public static final String nameColName = I18n.get("MissingForks.tableHeader.nameColName");
        public static final String userfolderColName = I18n.get("MissingForks.tableHeader.userfolderColName");
        public static final String daemonfolderColName = I18n.get("MissingForks.tableHeader.daemonfolderColName");
        public static final String unhide = I18n.get("MissingForks.tableHeader.unhide");

    }

    public static class ForkController {
        public static final String stagger = I18n.get("ForkController.menu.stagger");
        public static final String staggerDiagleLabel = I18n.get("ForkController.menu.stagger.diagleLabel");
        public static final String action = I18n.get("ForkController.menu.action");
        public static final String wallet = I18n.get("ForkController.menu.wallet");
        public static final String explore = I18n.get("ForkController.menu.explore");
        public static final String copy = I18n.get("ForkController.menu.copy");
        public static final String tools = I18n.get("ForkController.menu.tools");
        public static final String community = I18n.get("ForkController.menu.community");
        public static final String addColdWallet = I18n.get("ForkController.menu.addColdWallet");
        public static final String addFork = I18n.get("ForkController.menu.addFork");
        public static final String refresh = I18n.get("ForkController.menu.refresh");
        public static final String showPeers = I18n.get("ForkController.menu.showPeers");
        public static final String debug = I18n.get("ForkController.menu.debug");
        public static final String ffLogs = I18n.get("ForkController.menu.ffLogs");
        public static final String ffLogsTitle = I18n.get("ForkController.menu.ffLogs.title");
        public static final String showPeersTitleSuffix = I18n.get("ForkController.menu.showPeers.titleSuffix");
        public static final String actionStart = I18n.get("ForkController.menu.action.start");
        public static final String actionStop = I18n.get("ForkController.menu.action.stop");
        public static final String actionCustom = I18n.get("ForkController.menu.action.custom");
        public static final String actionActivate = I18n.get("ForkController.menu.action.activate");
        public static final String actionActivateCustom = I18n.get("ForkController.menu.action.ActivateCustom");
        public static final String actionEditStart = I18n.get("ForkController.menu.action.editStart");
        public static final String actionSetPassFile = I18n.get("ForkController.menu.action.setPassFile");
        public static final String actionHide = I18n.get("ForkController.menu.action.hide");
        public static final String homepage = I18n.get("ForkController.menu.community.homepage");
        public static final String discord = I18n.get("ForkController.menu.community.discord");
        public static final String github = I18n.get("ForkController.menu.community.github");
        public static final String twitter = I18n.get("ForkController.menu.community.twitter");
        public static final String calculator = I18n.get("ForkController.menu.community.calculator");
        public static final String xchforks = I18n.get("ForkController.menu.community.xchforks");
        public static final String alltheblocks = I18n.get("ForkController.menu.community.alltheblocks");
        public static final String forkschiaexchange = I18n.get("ForkController.menu.community.forkschiaexchange");
        public static final String chiaforksblockchain = I18n.get("ForkController.menu.community.chiaforksblockchain");
        public static final String casinoMaizeFarm = I18n.get("ForkController.menu.community.casinoMaizeFarm");
        public static final String multiWalletIndex = I18n.get("ForkController.menu.wallet.multiWalletIndex");
        public static final String walletAddColdWallet = I18n.get("ForkController.menu.wallet.addColdWallet");
        public static final String viewLog = I18n.get("ForkController.menu.explore.viewLog");
        public static final String openConfig = I18n.get("ForkController.menu.explore.openConfig");
        public static final String openCmd = I18n.get("ForkController.menu.explore.openCmd");
        public static final String openPowershell = I18n.get("ForkController.menu.explore.openPowershell");
        public static final String openTerminal = I18n.get("ForkController.menu.explore.openTerminal");
        public static final String copyAddress = I18n.get("ForkController.menu.copy.copyAddress");
        public static final String copyCSV = I18n.get("ForkController.menu.copy.copyCSV");
        public static final String ports = I18n.get("ForkController.menu.tools.ports");
        public static final String missing = I18n.get("ForkController.menu.tools.missing");
        public static final String missingFrameTitle = I18n.get("ForkController.menu.tools.missingFrameTitle");
        public static final String forceUpdate = I18n.get("ForkController.menu.tools.forceUpdate");
        public static final String forceUpdateTipText = I18n.get("ForkController.menu.tools.forceUpdate.tipText");
        public static final String plotCalc = I18n.get("ForkController.menu.tools.plotCalc");
        public static final String plotCalcFrameTitle = I18n.get("ForkController.menu.tools.plotCalcFrameTitle");
        public static final String portCheckerTitle = I18n.get("ForkController.menu.tools.ports.portCheckerTitle");
        public static final String addForktitle = I18n.get("ForkController.menu.addFork.title");
        public static final String addForkErrortitle = I18n.get("ForkController.menu.addFork.error.title");
        public static final String addForkErrorcontent = I18n.get("ForkController.menu.addFork.error.content");
        public static final String activateTitle = I18n.get("ForkController.menu.action.custom.activate.title");
        public static final String activatePopupTitle = I18n.get("ForkController.menu.action.custom.activate.popupTitle");
        public static final String addColdWalletTitle = I18n.get("ForkController.menu.wallet.addColdWallet.title");
        public static final String addCodeWalletLabel = I18n.get("ForkController.menu.wallet.addColdWallet.input.label");
        public static final String javaOldTipTitle = I18n.get("ForkController.javaOldTipTitle");

        public static final String javaOldTipContent(int jversion) {
            return I18n.getWithVariable("ForkController.javaOldTipContent", "" + jversion);
        }
    }

    public static class ForkStarter {
        public static final String customImmeditateRunLabel = I18n.get("ForkStarter.custom.immeditateRunLabel");
        public static final String customCmdPanelTitle = I18n.get("ForkStarter.custom.panelTitle");
        public static final String customCmdLabel = I18n.get("ForkStarter.custom.cmdLabel");
        public static final String customStaggerIntevalLabel = I18n.get("ForkStarter.custom.staggerIntevalLabel");
        public static final String editStartPanelTitle = I18n.get("ForkStarter.editStart.panelTitle");
        public static final String editStartTableTitle = I18n.get("ForkStarter.editStart.tableTitle");
    }

    public static class PortCheckerView {

    }

}
