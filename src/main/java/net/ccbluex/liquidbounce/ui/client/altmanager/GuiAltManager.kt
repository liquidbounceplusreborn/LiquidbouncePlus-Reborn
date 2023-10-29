/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.thealtening.AltService
import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.account.MojangAccount
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.LiquidBounce.fileManager
import net.ccbluex.liquidbounce.event.SessionEvent
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiChangeName
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiLoginIntoAccount
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiSessionLogin
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.altgenerator.GuiTheAltening
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.DictUtils
import net.ccbluex.liquidbounce.utils.login.LoginUtils
import net.ccbluex.liquidbounce.utils.login.UserUtils.isValidTokenOffline
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSlot
import net.minecraft.client.gui.GuiTextField
import net.minecraft.util.Session
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.thread

enum class BUTTON {
    ADD, REMOVE, IMPORT, EXPORT, COPY, REVERT, BACK, LOGIN, RANDOM, GENERATECRACKED, SEMIRANDOMFMT, CRACKED, DIRECTLOGIN, SESSIONLOGIN, CHANGENAME,
    THEALTENING,
}


class GuiAltManager(private val prevGui: GuiScreen) : GuiScreen() {

    var status = "§7Idle..."

    private lateinit var loginButton: GuiButton
    private lateinit var randomButton: GuiButton
    private lateinit var generatedCrackedButton: GuiButton
    private lateinit var altsList: GuiList
    private lateinit var searchField: GuiTextField
    private lateinit var revertOriginalAccount: GuiButton

    var lastSessionToken: String? = null

    override fun initGui() {
        val textFieldWidth = (width / 8).coerceAtLeast(70)
        searchField = GuiTextField(2, Fonts.fontSFUI40, width - textFieldWidth - 10, 10, textFieldWidth, 20)
        searchField.maxStringLength = Int.MAX_VALUE
        
        altsList = GuiList(this)
        altsList.registerScrollButtons(7, 8)
        
        val mightBeTheCurrentAccount = fileManager.accountsConfig.accounts.indexOfFirst { it.name == mc.session.username }
        altsList.elementClicked(mightBeTheCurrentAccount, false, 0, 0)
        altsList.scrollBy(mightBeTheCurrentAccount * altsList.getSlotHeight())

        // Setup buttons

        val startPositionY = 22

        buttonList.add(GuiButton(BUTTON.ADD.ordinal, width - 80, startPositionY + 24, 70, 20, "Add"))
        buttonList.add(GuiButton(BUTTON.REMOVE.ordinal, width - 80, startPositionY + 24 * 2, 70, 20, "Remove"))
        buttonList.add(GuiButton(BUTTON.IMPORT.ordinal, width - 80, startPositionY + 24 * 3, 70, 20, "Import"))
        buttonList.add(GuiButton(BUTTON.EXPORT.ordinal, width - 80, startPositionY + 24 * 4, 70, 20, "Export"))
        buttonList.add(GuiButton(BUTTON.COPY.ordinal, width - 80, startPositionY + 24 * 5, 70, 20, "Copy"))
        buttonList.add(GuiButton(BUTTON.REVERT.ordinal, width - 80, startPositionY + 24 * 6, 70, 20, "Revert").also { revertOriginalAccount = it })
        buttonList.add(GuiButton(BUTTON.BACK.ordinal, width - 80, height - 65, 70, 20, "Back"))

        buttonList.add(GuiButton(BUTTON.LOGIN.ordinal, 5, startPositionY + 24, 90, 20, "Login").also { loginButton = it })
        buttonList.add(GuiButton(BUTTON.RANDOM.ordinal, 5, startPositionY + 24 * 2, 90, 20, "Random Alt").also { randomButton = it })
        buttonList.add(GuiButton(BUTTON.GENERATECRACKED.ordinal, 5, startPositionY + 24 * 3, 90, 20, "Generate Cracked").also { generatedCrackedButton = it })
        buttonList.add(GuiButton(BUTTON.DIRECTLOGIN.ordinal, 5, startPositionY + 24 * 5, 90, 20, "Direct Login"))
        buttonList.add(GuiButton(BUTTON.SESSIONLOGIN.ordinal, 5, startPositionY + 24 * 6, 90, 20, "Session Login"))
        buttonList.add(GuiButton(BUTTON.CHANGENAME.ordinal, 5, startPositionY + 24 * 7, 90, 20, "Change Name"))

        if (activeGenerators.getOrDefault("thealtening", true))
            buttonList.add(GuiButton(BUTTON.THEALTENING.ordinal, 5, startPositionY + 24 * 8, 90, 20, "TheAltening"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        revertOriginalAccount.enabled = (lastSessionToken != null)
        drawBackground(0)
        altsList.drawScreen(mouseX, mouseY, partialTicks)
        Fonts.fontSFUI40.drawCenteredString("AltManager", width / 2.0f, 6f, 0xffffff)
        Fonts.fontSFUI35.drawCenteredString(
            if (searchField.text.isEmpty()) "${fileManager.accountsConfig.accounts.size} Alts" else altsList.accounts.size.toString() + " Search Results",
            width / 2.0f,
            18f,
            0xffffff
        )
        Fonts.fontSFUI35.drawCenteredString(status, width / 2.0f, 32f, 0xffffff)
        Fonts.fontSFUI35.drawStringWithShadow(
            "§7User: §a${mc.getSession().username}",
            6f,
            6f,
            0xffffff
        )
        Fonts.fontSFUI35.drawStringWithShadow("§7Type: §a${if (altService.currentService == AltService.EnumAltService.THEALTENING) "TheAltening" else if (isValidTokenOffline(mc.getSession().token)) "Premium" else "Cracked"}", 6f, 15f, 0xffffff)
        searchField.drawTextBox()
        if (searchField.text.isEmpty() && !searchField.isFocused) Fonts.fontSFUI40.drawStringWithShadow(
            "§7Search...",
            (searchField.xPosition + 4).toFloat(),
            17f,
            0xffffff
        )
        generateCracked.drawTextBox()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    public override fun actionPerformed(button: GuiButton) {
        // Not enabled buttons should be ignored
        if (!button.enabled)
            return

        when (button.id) {
            BUTTON.BACK.ordinal -> mc.displayGuiScreen(prevGui)
            BUTTON.ADD.ordinal -> mc.displayGuiScreen(GuiLoginIntoAccount(this))
            BUTTON.REMOVE.ordinal -> {
                status = if (altsList.selectedSlot != -1 && altsList.selectedSlot < altsList.size) {
                    fileManager.accountsConfig.removeAccount(altsList.accounts[altsList.selectedSlot])
                    fileManager.saveConfig(fileManager.accountsConfig)
                    "§aThe account has been removed."
                } else {
                    "§cSelect an account."
                }
            }

            BUTTON.LOGIN.ordinal -> {
                if (lastSessionToken == null)
                    lastSessionToken = mc.session.token

                status = altsList.selectedAccount?.let {
                    loginButton.enabled = false
                    randomButton.enabled = false
                    generatedCrackedButton.enabled = false

                    login(it, {
                        status = "§aLogged into ${mc.session.username}."
                    },{ exception ->
                        status = "§cLogin failed due to '${exception.message}'."
                    },{
                        loginButton.enabled = true
                        randomButton.enabled = true
                        generatedCrackedButton.enabled = true
                    })

                    "§aLogging in..."
                } ?: "§cSelect an account."
            }

            BUTTON.RANDOM.ordinal -> {
                if (lastSessionToken == null)
                    lastSessionToken = mc.session.token

                status = altsList.accounts.randomOrNull()?.let {
                    loginButton.enabled = false
                    randomButton.enabled = false
                    generatedCrackedButton.enabled = false

                    login(it, {
                        status = "§aLogged into ${mc.session.username}."
                    },{ exception ->
                        status = "§cLogin failed due to '${exception.message}'."
                    },{
                        loginButton.enabled = true
                        randomButton.enabled = true
                        generatedCrackedButton.enabled = true
                    })

                    "§aLogging in..."
                } ?: "§cYou do not have any accounts."
            }

            BUTTON.CRACKED.ordinal -> {
                if (lastSessionToken == null)
                    lastSessionToken = mc.session.token

                loginButton.enabled = false
                randomButton.enabled = false
                generatedCrackedButton.enabled = false

                val rand = CrackedAccount()
                rand.name = RandomUtils.randomString(RandomUtils.nextInt(5, 16))

                status = "§aGenerating..."

                login(rand, {
                    status = "§aLogged in as ${mc.session.username}."
                }, { exception ->
                    status = "§cLogin failed due to '${exception.message}'."
                }, {
                    loginButton.enabled = true
                    randomButton.enabled = true
                    generatedCrackedButton.enabled = true
                })
            }

            BUTTON.GENERATECRACKED.ordinal -> {
                if (lastSessionToken == null)
                    lastSessionToken = mc.session.token

                loginButton.enabled = false
                randomButton.enabled = false
                generatedCrackedButton.enabled = false

                val rand = CrackedAccount()
                rand.name = DictUtils.get(generateCracked.text)

                status = "§aGenerating..."

                login(rand, {
                    status = "§aLogged in as ${mc.session.username}."
                }, { exception ->
                    status = "§cLogin failed due to '${exception.message}'."
                }, {
                    loginButton.enabled = true
                    randomButton.enabled = true
                    generatedCrackedButton.enabled = true
                })
            }

            BUTTON.DIRECTLOGIN.ordinal -> { // Direct login button
                mc.displayGuiScreen(GuiLoginIntoAccount(this, directLogin = true))
            }

            BUTTON.IMPORT.ordinal -> { // Import button
                val file = MiscUtils.openFileChooser() ?: return

                file.readLines().forEach {
                    val accountData = it.split(":".toRegex(), limit = 2)
                    if (accountData.size > 1) {
                        // Most likely mojang account
                        fileManager.accountsConfig.addMojangAccount(accountData[0], accountData[1])
                    } else if (accountData[0].length < 16) {
                        // Might be cracked account
                        fileManager.accountsConfig.addCrackedAccount(accountData[0])
                    } // skip account
                }

                fileManager.saveConfig(fileManager.accountsConfig)
                status = "§aThe accounts were imported successfully."
            }
            BUTTON.EXPORT.ordinal -> { // Export button
                if (fileManager.accountsConfig.accounts.isEmpty()) {
                    status = "§cYou do not have any accounts to export."
                    return
                }

                val file = MiscUtils.saveFileChooser()
                if (file == null || file.isDirectory) {
                    return
                }

                try {
                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    val accounts = fileManager.accountsConfig.accounts.joinToString(separator = "\n") { account ->
                        when (account) {
                            is MojangAccount -> "${account.email}:${account.password}" // EMAIL:PASSWORD
                            is MicrosoftAccount -> "${account.name}:${account.session.token}" // NAME:SESSION
                            else -> account.name
                        }
                    }
                    file.writeText(accounts)

                    status = "§aExported successfully!"
                } catch (e: Exception) {
                    status = "§cUnable to export due to error: ${e.message}"
                }
            }
            BUTTON.COPY.ordinal -> {
                val currentAccount = altsList.selectedAccount

                if (currentAccount == null) {
                    status = "§cSelect an account."
                    return
                }

                // Format data for other tools
                val formattedData = when (currentAccount) {
                    is MojangAccount -> "${currentAccount.email}:${currentAccount.password}" // EMAIL:PASSWORD
                    is MicrosoftAccount -> "${currentAccount.name}:${currentAccount.session.token}" // NAME:SESSION
                    else -> currentAccount.name
                }

                // Copy to clipboard
                Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(formattedData), null)
                status = "§aCopied account into your clipboard."
            }

            BUTTON.CHANGENAME.ordinal -> { // Gui Change Name Button
                mc.displayGuiScreen(GuiChangeName(this))
            }

            BUTTON.THEALTENING.ordinal -> { // Altening Button
                mc.displayGuiScreen(GuiTheAltening(this))
            }

            BUTTON.SESSIONLOGIN.ordinal -> { // Session Login Button
                mc.displayGuiScreen(GuiSessionLogin(this))
            }

            BUTTON.REVERT.ordinal -> {
                loginButton.enabled = false
                randomButton.enabled = false
                generatedCrackedButton.enabled = false
                status = "§aLogging in..."

                thread {
                    val loginResult = LoginUtils.loginSessionId(lastSessionToken!!)

                    status = when (loginResult) {
                        LoginUtils.LoginResult.LOGGED -> {
                            if (altService.currentService != AltService.EnumAltService.MOJANG) {
                                try {
                                    altService.switchService(AltService.EnumAltService.MOJANG)
                                } catch (e: NoSuchFieldException) {
                                    ClientUtils.getLogger().error("Something went wrong while trying to switch alt service.", e)
                                } catch (e: IllegalAccessException) {
                                    ClientUtils.getLogger().error("Something went wrong while trying to switch alt service.", e)
                                }
                            }

                            "§cYour name is now §f§l${mc.session.username}§c"
                        }
                        LoginUtils.LoginResult.FAILED_PARSE_TOKEN -> "§cFailed to parse Session ID!"
                        LoginUtils.LoginResult.INVALID_ACCOUNT_DATA -> "§cInvalid Session ID!"
                        else -> ""
                    }

                    loginButton.enabled = true
                    randomButton.enabled = true
                    generatedCrackedButton.enabled = true

                    lastSessionToken = null
                }
            }
        }
    }

    public override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (searchField.isFocused) {
            searchField.textboxKeyTyped(typedChar, keyCode)
        }

        if (generateCracked.isFocused)
            generateCracked.textboxKeyTyped(typedChar, keyCode)

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> { // Go back
                mc.displayGuiScreen(prevGui)
                return
            }
            Keyboard.KEY_UP -> { // Go one up in account list
                var i = altsList.selectedSlot - 1
                if (i < 0) i = 0
                altsList.elementClicked(i, false, 0, 0)
            }
            Keyboard.KEY_DOWN -> { // Go one down in account list
                var i = altsList.selectedSlot + 1
                if (i >= altsList.size) i = altsList.size - 1
                altsList.elementClicked(i, false, 0, 0)
            }
            Keyboard.KEY_RETURN -> { // Login into account
                altsList.elementClicked(altsList.selectedSlot, true, 0, 0)
            }
            Keyboard.KEY_NEXT -> { // Scroll account list
                altsList.scrollBy(height - 100)
            }
            Keyboard.KEY_PRIOR -> { // Scroll account list
                altsList.scrollBy(-height + 100)
                return
            }
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        altsList.handleMouseInput()
    }

    public override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton)
        generateCracked.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() {
        searchField.updateCursorCounter()
        generateCracked.updateCursorCounter()
    }

    private inner class GuiList(prevGui: GuiScreen) : GuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {

        val accounts: List<MinecraftAccount>
            get() {
                var search = searchField.text
                if (search == null || search.isEmpty()) {
                    return fileManager.accountsConfig.accounts
                }
                search = search.lowercase(Locale.getDefault())

                return fileManager.accountsConfig.accounts.filter { it.name.contains(search, ignoreCase = true) || (it is MojangAccount && it.email.contains(search, ignoreCase = true)) }
            }

        var selectedSlot = 0
            get() {
                return if (field > accounts.size) {
                    -1
                } else {
                    field
                }
            }

        val selectedAccount: MinecraftAccount?
            get() = if (selectedSlot >= 0 && selectedSlot < accounts.size) {
                accounts[selectedSlot]
            } else {
                null
            }

        override fun isSelected(id: Int) = selectedSlot == id

        public override fun getSize() = accounts.size

        public override fun elementClicked(clickedElement: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = clickedElement

            if (doubleClick) {
                status = altsList.selectedAccount?.let {
                    loginButton.enabled = false
                    randomButton.enabled = false
                    generatedCrackedButton.enabled = false

                    login(it, {
                        status = "§aLogged into ${mc.session.username}."
                    },{ exception ->
                        status = "§cLogin failed due to '${exception.message}'."
                    },{
                        loginButton.enabled = true
                        randomButton.enabled = true
                        generatedCrackedButton.enabled = true
                    })

                    "§aLogging in..."
                } ?: "§cSelect an account."
            }
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val minecraftAccount = accounts[id]
            val accountName = if (minecraftAccount is MojangAccount && minecraftAccount.name.isEmpty()) {
                minecraftAccount.email
            } else {
                minecraftAccount.name
            }

            Fonts.fontSFUI40.drawCenteredString(accountName, width / 2f, y + 2f, Color.WHITE.rgb, true)
            Fonts.fontSFUI40.drawCenteredString(if (minecraftAccount is CrackedAccount) "Cracked" else if (minecraftAccount is MicrosoftAccount) "Microsoft" else if (minecraftAccount is MojangAccount) "Mojang" else "Something else", width / 2f, y + 15f, if (minecraftAccount is CrackedAccount) Color.GRAY.rgb else Color(118, 255, 95).rgb, true)
        }

        override fun drawBackground() { }
    }

    companion object {

        val altService = AltService()
        private val activeGenerators = mutableMapOf<String, Boolean>()
        var generateCracked: GuiTextField =
            GuiTextField(BUTTON.SEMIRANDOMFMT.ordinal, Fonts.minecraftFont, 5, 22 + 24 * 4 + 1, 90, 18)

        init {
            generateCracked.text = "%W%W%d%d%d%d"
            generateCracked.maxStringLength = 100
        }

        fun loadActiveGenerators() {
            try {
                // Read versions json from cloud
                val jsonElement = JsonParser().parse(get(LiquidBounce.CLIENT_CLOUD + "/generators.json"))

                // Check json is valid object
                if (jsonElement.isJsonObject) {
                    // Get json object of element
                    val jsonObject = jsonElement.asJsonObject
                    jsonObject.entrySet().forEach(Consumer { (key, value): Map.Entry<String, JsonElement> ->
                        activeGenerators[key] = value.asBoolean
                    })
                }
            } catch (throwable: Throwable) {
                // Print throwable to console
                ClientUtils.getLogger().error("Failed to load enabled generators.", throwable)
            }
        }

        fun login(minecraftAccount: MinecraftAccount, success: () -> Unit, error: (Exception) -> Unit, done: () -> Unit) = thread(name = "LoginTask") {
            if (altService.currentService != AltService.EnumAltService.MOJANG) {
                try {
                    altService.switchService(AltService.EnumAltService.MOJANG)
                } catch (e: NoSuchFieldException) {
                    error(e)
                    ClientUtils.getLogger().error("Something went wrong while trying to switch alt service.", e)
                } catch (e: IllegalAccessException) {
                    error(e)
                    ClientUtils.getLogger().error("Something went wrong while trying to switch alt service.", e)
                }
            }

            try {
                minecraftAccount.update()
                Minecraft.getMinecraft().session = Session(
                    minecraftAccount.session.username,
                    minecraftAccount.session.uuid, minecraftAccount.session.token, "mojang"
                )
                LiquidBounce.eventManager.callEvent(SessionEvent())

                success()
            } catch (exception: Exception) {
                error(exception)
            }
            done()
        }
    }
}