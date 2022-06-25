import Checkbox from "../../components/CustomCheckbox"
import NumericInput from "react-native-numeric-input"
import React, { useContext, useState } from "react"
import TitleDivider from "../../components/TitleDivider"
import { BotStateContext } from "../../context/BotStateContext"
import { Dimensions, ScrollView, StyleSheet, View } from "react-native"
import { Text } from "react-native-elements"
import { Slider } from "@sharcoux/slider"
import SnackBar from "rn-snackbar-component"
import MIcon from "react-native-vector-icons/MaterialCommunityIcons"

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
    const [modalOpen, setModalOpen] = useState<boolean>(false)
    const [showSnackbar, setShowSnackbar] = useState<boolean>(false)
    const [testInProgress, setTestInProgress] = useState<boolean>(false)
    const [testFailed, setTestFailed] = useState<boolean>(false)
    const [testErrorMessage, setTestErrorMessage] = useState<string>("")

    const bsc = useContext(BotStateContext)

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Rendering

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

    return (
        <View style={styles.root}>
            <ScrollView>{renderDeviceSettings()}</ScrollView>

            <SnackBar
                visible={showSnackbar}
                message={testFailed ? testErrorMessage : "Test was successful."}
                actionHandler={() => setShowSnackbar(false)}
                action={<MIcon name="close" size={25} />}
                autoHidingTime={10000}
                containerStyle={{ backgroundColor: testFailed ? "red" : "green", borderRadius: 10 }}
                native={false}
            />
        </View>
    )
}

export default ExtraSettings
