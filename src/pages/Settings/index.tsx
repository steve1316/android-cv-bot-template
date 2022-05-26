import Ionicons from "react-native-vector-icons/Ionicons"
import React, { useContext, useEffect, useState } from "react"
import SnackBar from "rn-snackbar-component"
import { BotStateContext } from "../../context/BotStateContext"
import { ScrollView, StyleSheet, View } from "react-native"
import { Divider } from "react-native-elements"
import CustomCheckbox from "../../components/CustomCheckbox"

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

    return (
        <View style={styles.root}>
            <CustomCheckbox
                isChecked={bsc.settings.property1}
                onPress={() => bsc.setSettings({ ...bsc.settings, property1: !bsc.settings.property1 })}
                text="I am a Checkbox"
                subtitle="Check this to enable the Start button on the Home Page"
            />

            <SnackBar
                visible={snackbarOpen}
                message={bsc.readyStatus ? "Bot is ready!" : "Bot is not ready!"}
                actionHandler={() => setSnackbarOpen(false)}
                action={<Ionicons name="close" size={30} />}
                autoHidingTime={1500}
                containerStyle={{ backgroundColor: bsc.readyStatus ? "green" : "red", borderRadius: 10 }}
                native={false}
            />

            <ScrollView nestedScrollEnabled={true} contentContainerStyle={{ flexGrow: 1 }}>
                <View style={{ marginHorizontal: 20 }}>
                    <Divider />
                </View>
            </ScrollView>
        </View>
    )
}

export default Settings
