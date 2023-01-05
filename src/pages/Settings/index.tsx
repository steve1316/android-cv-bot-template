import React, { useContext, useEffect, useState } from "react"
import { Snackbar } from "react-native-paper"
import { BotStateContext } from "../../context/BotStateContext"
import { ScrollView, StyleSheet, View } from "react-native"
import CustomCheckbox from "../../components/CustomCheckbox"
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

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Callbacks

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

    return (
        <View style={styles.root}>
            <ScrollView>{renderSampleSettings()}</ScrollView>

            <Snackbar
                visible={snackbarOpen}
                onDismiss={() => setSnackbarOpen(false)}
                action={{
                    label: "Close",
                    onPress: () => {
                        setSnackbarOpen(false)
                    },
                }}
                duration={1500}
                style={{ backgroundColor: bsc.readyStatus ? "green" : "red", borderRadius: 10 }}
            >
                {bsc.readyStatus ? "Bot is ready!" : "Bot is not ready!"}
            </Snackbar>
        </View>
    )
}

export default Settings
