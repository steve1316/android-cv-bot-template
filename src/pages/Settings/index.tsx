import Checkbox from "../../components/Checkbox"
import CustomButton from "../../components/CustomButton"
import DocumentPicker from "react-native-document-picker"
import DropDownPicker, { ValueType } from "react-native-dropdown-picker"
import Ionicons from "react-native-vector-icons/Ionicons"
import React, { useContext, useEffect, useState } from "react"
import RNFS from "react-native-fs"
import SnackBar from "rn-snackbar-component"
import { BotStateContext } from "../../context/BotStateContext"
import { Dimensions, Modal, ScrollView, StyleSheet, Text, TouchableOpacity, View } from "react-native"
import { Divider } from "react-native-elements"
import { Picker } from "@react-native-picker/picker"

const styles = StyleSheet.create({
    root: {
        flex: 1,
        flexDirection: "column",
        justifyContent: "center",
        margin: 10,
    },
    picker: {
        marginVertical: 10,
        backgroundColor: "azure",
    },
    dropdown: {
        marginTop: 20,
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

interface Item {
    label: string
    value: string
}

export interface CombatScript {
    name: string
    script: string[]
}

const Settings = () => {
    const [isFarmingModePickerOpen, setIsFarmingModePickerOpen] = useState<boolean>(false)
    const [isItemPickerOpen, setIsItemPickerOpen] = useState<boolean>(false)
    const [isMissionPickerOpen, setIsMissionPickerOpen] = useState<boolean>(false)
    const [modalOpen, setModalOpen] = useState<boolean>(false)
    const [snackbarOpen, setSnackbarOpen] = useState<boolean>(false)

    // Certain individual states are necessary as react-native-dropdown-picker requires a setValue state parameter for DropDownPicker.
    const [farmingMode, setFarmingMode] = useState<ValueType | null>("")
    const [item, setItem] = useState<ValueType | null>("")
    const [mission, setMission] = useState<ValueType | null>("")

    const bsc = useContext(BotStateContext)

    // Manually close all pickers as react-native-dropdown-picker does not handle that automatically.
    const closeAllPickers = () => {
        setIsFarmingModePickerOpen(false)
        setIsItemPickerOpen(false)
        setIsMissionPickerOpen(false)
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    // Rendering

    return (
        <View style={styles.root}>
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
