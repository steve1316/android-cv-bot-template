import Ionicons from "react-native-vector-icons/Ionicons"
import React, { useContext, useEffect, useState } from "react"
import SnackBar from "rn-snackbar-component"
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
