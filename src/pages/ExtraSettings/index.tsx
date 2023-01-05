import Checkbox from "../../components/CustomCheckbox"
import LoadingButton from "../../components/LoadingButton"
import NumericInput from "react-native-numeric-input"
import React, { useContext, useEffect, useState } from "react"
import { Snackbar } from "react-native-paper"
import TitleDivider from "../../components/TitleDivider"
import { BotStateContext } from "../../context/BotStateContext"
import { DeviceEventEmitter, Dimensions, ScrollView, StyleSheet, View } from "react-native"
import { Input, Text } from "react-native-elements"
import { NativeModules } from "react-native" // Import native Java module.
import { Slider } from "@sharcoux/slider"

const styles = StyleSheet.create({
    root: {
        flex: 1,
        flexDirection: "column",
        justifyContent: "center",
        margin: 10,
    },
    modal: {
        flex: 1,
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "rgba(80,80,80,0.3)",
    },
    outsideModal: {
        position: "absolute",
        height: "100%",
        width: "100%",
    },
    componentContainer: {
        width: Dimensions.get("window").width * 0.7,
        height: Dimensions.get("window").height * 0.9,
    },
})

const ExtraSettings = () => {
    const [showSnackbar, setShowSnackbar] = useState<boolean>(false)
    const [testInProgress, setTestInProgress] = useState<boolean>(false)
    const [testFailed, setTestFailed] = useState<boolean>(false)
    const [testErrorMessage, setTestErrorMessage] = useState<string>("")

    const bsc = useContext(BotStateContext)

    const { StartModule } = NativeModules

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Callbacks

    useEffect(() => {
        if (showSnackbar) {
            setTimeout(() => {
                setShowSnackbar(false)
            }, 10000)
        }
    }, [showSnackbar])

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Rendering

    const renderTwitterSettings = () => {
        return (
            <View>
                <TitleDivider
                    title="Twitter Settings"
                    subtitle="Please visit the wiki on the GitHub page for instructions on how to get these keys and tokens. In addition, API v1.1 is supported but not API v2."
                    hasIcon={true}
                    iconName="twitter"
                    iconColor="#1da1f2"
                />

                <Input
                    label="Twitter API Key"
                    multiline
                    containerStyle={{ marginLeft: -10 }}
                    value={bsc.settings.twitter.twitterAPIKey}
                    onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, twitter: { ...bsc.settings.twitter, twitterAPIKey: value } })}
                />
                <Input
                    label="Twitter API Key Secret"
                    multiline
                    containerStyle={{ marginLeft: -10 }}
                    value={bsc.settings.twitter.twitterAPIKeySecret}
                    onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, twitter: { ...bsc.settings.twitter, twitterAPIKeySecret: value } })}
                />
                <Input
                    label="Twitter Access Token"
                    multiline
                    containerStyle={{ marginLeft: -10 }}
                    value={bsc.settings.twitter.twitterAccessToken}
                    onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, twitter: { ...bsc.settings.twitter, twitterAccessToken: value } })}
                />
                <Input
                    label="Twitter Access Token Secret"
                    multiline
                    containerStyle={{ marginLeft: -10 }}
                    value={bsc.settings.twitter.twitterAccessTokenSecret}
                    onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, twitter: { ...bsc.settings.twitter, twitterAccessTokenSecret: value } })}
                />
                <LoadingButton title="Test Twitter API v1.1" loadingTitle="In progress..." isLoading={testInProgress} onPress={() => testTwitter()} />
            </View>
        )
    }

    const renderDiscordSettings = () => {
        return (
            <View>
                <TitleDivider
                    title="Discord Settings"
                    subtitle={`Please visit the wiki on the GitHub page for instructions on how to get the token and user ID.`}
                    hasIcon={true}
                    iconName="discord"
                    iconColor="#7289d9"
                />
                <Checkbox
                    text="Enable Discord Notifications"
                    subtitle="Enable notifications of loot drops and errors encountered by the bot via Discord DMs."
                    isChecked={bsc.settings.discord.enableDiscordNotifications}
                    onPress={() => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, enableDiscordNotifications: !bsc.settings.discord.enableDiscordNotifications } })}
                />
                {bsc.settings.discord.enableDiscordNotifications ? (
                    <View>
                        <Input
                            label="Discord Token"
                            multiline
                            containerStyle={{ marginLeft: -10 }}
                            value={bsc.settings.discord.discordToken}
                            onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, discordToken: value } })}
                        />
                        <Input
                            label="Discord User ID"
                            multiline
                            containerStyle={{ marginLeft: -10 }}
                            value={bsc.settings.discord.discordUserID}
                            onChangeText={(value: string) => bsc.setSettings({ ...bsc.settings, discord: { ...bsc.settings.discord, discordUserID: value } })}
                        />
                        <LoadingButton title="Test Discord API" loadingTitle="In progress..." isLoading={testInProgress} onPress={() => testDiscord()} />
                    </View>
                ) : null}
            </View>
        )
    }

    const renderDeviceSettings = () => {
        return (
            <View>
                <TitleDivider
                    title="Device Settings"
                    subtitle={`Adjust and fine-tune settings related to device setups and image processing optimizations.`}
                    hasIcon={true}
                    iconName="tablet-cellphone"
                />
                <Text style={{ marginBottom: 10 }}>Set Confidence Level: {bsc.settings.android.confidence}%</Text>
                <NumericInput
                    type="plus-minus"
                    leftButtonBackgroundColor="#eb5056"
                    rightButtonBackgroundColor="#EA3788"
                    rounded
                    valueType="integer"
                    minValue={1}
                    maxValue={100}
                    value={bsc.settings.android.confidence}
                    onChange={(value) => bsc.setSettings({ ...bsc.settings, android: { ...bsc.settings.android, confidence: value } })}
                    containerStyle={{ marginBottom: 10, alignSelf: "center" }}
                    totalWidth={Dimensions.get("screen").width * 0.9}
                    totalHeight={50}
                />
                <Text style={{ marginBottom: 10 }}>Set Confidence Level for Multiple Matching: {bsc.settings.android.confidenceAll}%</Text>
                <NumericInput
                    type="plus-minus"
                    leftButtonBackgroundColor="#eb5056"
                    rightButtonBackgroundColor="#EA3788"
                    rounded
                    valueType="integer"
                    minValue={1}
                    maxValue={100}
                    value={bsc.settings.android.confidenceAll}
                    onChange={(value) => bsc.setSettings({ ...bsc.settings, android: { ...bsc.settings.android, confidenceAll: value } })}
                    containerStyle={{ marginBottom: 10, alignSelf: "center" }}
                    totalWidth={Dimensions.get("screen").width * 0.9}
                    totalHeight={50}
                />
                <Text>Set Custom Scale: {bsc.settings.android.customScale % 1 === 0 ? `${bsc.settings.android.customScale}.0` : bsc.settings.android.customScale}</Text>
                <Text style={{ marginBottom: 10, fontSize: 12, opacity: 0.7 }}>
                    Set the scale at which to resize existing image assets to match what would be shown on your device. Internally supported are 720p, 1080p, 1600p (Portrait) and 2560p (Landscape)
                    mode.
                </Text>
                <NumericInput
                    type="plus-minus"
                    leftButtonBackgroundColor="#eb5056"
                    rightButtonBackgroundColor="#EA3788"
                    rounded
                    valueType="real"
                    minValue={0.1}
                    maxValue={5.0}
                    step={0.1}
                    value={bsc.settings.android.customScale}
                    onChange={(value) => bsc.setSettings({ ...bsc.settings, android: { ...bsc.settings.android, customScale: value } })}
                    containerStyle={{ marginBottom: 10, alignSelf: "center" }}
                    totalWidth={Dimensions.get("screen").width * 0.9}
                    totalHeight={50}
                />
                <Checkbox
                    text="Enable Additional Delay Before Tap"
                    subtitle="Enables a range of delay before each tap in milliseconds (ms). The base point will be used to create a range from -100ms to +100ms using it to determine the additional delay."
                    isChecked={bsc.settings.android.enableDelayTap}
                    onPress={() => bsc.setSettings({ ...bsc.settings, android: { ...bsc.settings.android, enableDelayTap: !bsc.settings.android.enableDelayTap } })}
                />
                {bsc.settings.android.enableDelayTap ? (
                    <View>
                        <Text style={{ marginBottom: 10 }}>Set Base Point for Additional Delay: {bsc.settings.android.delayTapMilliseconds} milliseconds</Text>
                        <Slider
                            value={bsc.settings.android.delayTapMilliseconds}
                            minimumValue={1000}
                            maximumValue={5000}
                            step={100}
                            onSlidingComplete={(value) => bsc.setSettings({ ...bsc.settings, android: { ...bsc.settings.android, delayTapMilliseconds: value } })}
                            minimumTrackTintColor="black"
                            maximumTrackTintColor="gray"
                            thumbTintColor="teal"
                            thumbSize={25}
                            trackHeight={10}
                            style={{ width: "95%", alignSelf: "center", marginBottom: 10 }}
                        />
                    </View>
                ) : null}
            </View>
        )
    }

    const testTwitter = () => {
        // Add listener to work around the UI freezing issue associated with Javacord blocking the thread.
        DeviceEventEmitter.addListener("testTwitter", (data) => {
            let result: string = data["message"]
            if (result !== "Test successfully completed.") {
                setTestFailed(true)
                setTestErrorMessage(result)
            } else {
                setTestFailed(false)
            }

            setTestInProgress(false)
            setShowSnackbar(true)
        })

        setTestInProgress(true)
        StartModule.startTwitterTest()
    }

    const testDiscord = () => {
        // Add listener to work around the UI freezing issue associated with Javacord blocking the thread.
        DeviceEventEmitter.addListener("testDiscord", (data) => {
            let result: string = data["message"]
            if (result !== "Test successfully completed.") {
                setTestFailed(true)
                setTestErrorMessage(result)
            } else {
                setTestFailed(false)
            }

            setTestInProgress(false)
            setShowSnackbar(true)
        })

        setTestInProgress(true)
        StartModule.startDiscordTest()
    }

    return (
        <View style={styles.root}>
            <ScrollView>
                {renderTwitterSettings()}

                {renderDiscordSettings()}

                {renderDeviceSettings()}
            </ScrollView>

            <Snackbar
                visible={showSnackbar}
                onDismiss={() => setShowSnackbar(false)}
                action={{
                    label: "Close",
                    onPress: () => {
                        setShowSnackbar(false)
                    },
                }}
                duration={10000}
                style={{ backgroundColor: testFailed ? "red" : "green", borderRadius: 10 }}
            >
                {testFailed ? testErrorMessage : "Test was successful."}
            </Snackbar>
        </View>
    )
}

export default ExtraSettings
