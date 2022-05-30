import Ionicons from "react-native-vector-icons/Ionicons"
import React, { useContext, useEffect, useState } from "react"
import SnackBar from "rn-snackbar-component"
import { BotStateContext } from "../../context/BotStateContext"
import { ScrollView, StyleSheet, View } from "react-native"
import CustomCheckbox from "../../components/CustomCheckbox"
import { TextInput } from "react-native-paper"
import TitleDivider from "../../components/TitleDivider"

const styles = StyleSheet.create({
    root: {
        flex: 1,
        flexDirection: "column",
        justifyContent: "center",
        margin: 10,
    },
})

const Settings = () => {
    const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false)

    const bsc = useContext(BotStateContext)

    useEffect(() => {
        // Manually set this flag to false as the snackbar autohiding does not set this to false automatically.
        setSnackbarOpen(true)
        setTimeout(() => setSnackbarOpen(false), 1500)
    }, [bsc.readyStatus])

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Rendering

    const renderSampleSettings = () => {
        return (
            <View>
                <TitleDivider title="Sample Settings" subtitle="Sample description 1" hasIcon={true} iconName="clipboard-account-outline" iconColor="#000" />

                <CustomCheckbox
                    isChecked={bsc.settings.property1}
                    onPress={() => bsc.setSettings({ ...bsc.settings, property1: !bsc.settings.property1 })}
                    text="I am a Checkbox"
                    subtitle="Check this to enable the Start button on the Home Page"
                />
            </View>
        )
    }

    const renderDiscordSettings = () => {
        return (
            <View>
                <TitleDivider title="Discord Settings" subtitle="Sample description 2" hasIcon={true} iconName="discord" iconColor="#7289d9" />

                <CustomCheckbox
                    isChecked={bsc.settings.discord.enableDiscordNotifications}
                    onPress={() => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, enableDiscordNotifications: !bsc.settings.discord.enableDiscordNotifications } })}
                    text="Enable Discord Notifications"
                    subtitle="Check this to enable having the bot send you status notifications via Discord DM."
                />

                {bsc.settings.discord.enableDiscordNotifications ? (
                    <View>
                        <TextInput
                            label="Discord Token"
                            mode="outlined"
                            multiline
                            right={<TextInput.Icon name="close" onPress={() => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, discordToken: "" } })} />}
                            value={bsc.settings.discord.discordToken}
                            onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, discordToken: value } })}
                            autoComplete={false}
                        />

                        <TextInput
                            label="Discord User ID"
                            mode="outlined"
                            multiline
                            right={<TextInput.Icon name="close" onPress={() => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, discordUserID: "" } })} />}
                            value={bsc.settings.discord.discordUserID}
                            onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, discordUserID: value } })}
                            autoComplete={false}
                        />
                    </View>
                ) : null}
            </View>
        )
    }

    return (
        <View style={styles.root}>
            <ScrollView>
                {renderSampleSettings()}

                {renderDiscordSettings()}
            </ScrollView>

            <SnackBar
                visible={snackbarOpen}
                message={bsc.readyStatus ? "Bot is ready!" : "Bot is not ready!"}
                actionHandler={() => setSnackbarOpen(false)}
                action={<Ionicons name="close" size={30} />}
                autoHidingTime={1500}
                containerStyle={{ backgroundColor: bsc.readyStatus ? "green" : "red", borderRadius: 10 }}
                native={false}
            />
        </View>
    )
}

export default Settings
