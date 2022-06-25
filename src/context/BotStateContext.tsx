import React, { createContext, useState } from "react"

export interface Settings {
    property1: boolean

    discord: {
        enableDiscordNotifications: boolean
        discordToken: string
        discordUserID: string
    }

    android: {
        enableDelayTap: boolean
        delayTapMilliseconds: number
        confidence: number
        confidenceAll: number
        customScale: number
        enableTestForHomeScreen: boolean
    }
}

// Set the default settings.
export const defaultSettings: Settings = {
    property1: false,
    discord: {
        enableDiscordNotifications: false,
        discordToken: "",
        discordUserID: "",
    },
    android: {
        enableDelayTap: false,
        delayTapMilliseconds: 1000,
        confidence: 80,
        confidenceAll: 80,
        customScale: 1.0,
        enableTestForHomeScreen: false,
    },
}

interface IProviderProps {
    readyStatus: boolean
    setReadyStatus: (readyStatus: boolean) => void
    isBotRunning: boolean
    setIsBotRunning: (isBotRunning: boolean) => void
    startBot: boolean
    setStartBot: (startBot: boolean) => void
    stopBot: boolean
    setStopBot: (stopBot: boolean) => void
    settings: Settings
    setSettings: (settings: Settings) => void
}

export const BotStateContext = createContext<IProviderProps>({} as IProviderProps)

// https://stackoverflow.com/a/60130448 and https://stackoverflow.com/a/60198351
export const BotStateProvider = ({ children }: any): JSX.Element => {
    const [readyStatus, setReadyStatus] = useState<boolean>(false)
    const [isBotRunning, setIsBotRunning] = useState<boolean>(false)
    const [startBot, setStartBot] = useState<boolean>(false)
    const [stopBot, setStopBot] = useState<boolean>(false)

    const [settings, setSettings] = useState<Settings>(defaultSettings)

    const providerValues: IProviderProps = {
        readyStatus,
        setReadyStatus,
        isBotRunning,
        setIsBotRunning,
        startBot,
        setStartBot,
        stopBot,
        setStopBot,
        settings,
        setSettings,
    }

    return <BotStateContext.Provider value={providerValues}>{children}</BotStateContext.Provider>
}
