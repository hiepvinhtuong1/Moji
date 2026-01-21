import Test from "@/components/auth/Test"
import ChatWindowLayout from "@/components/chat/ChatWindowLayout"
import { AppSidebar } from "@/components/sidebar/app-sidebar"
import { SidebarProvider } from "@/components/ui/sidebar"

const ChatAppPage = () => {
    return (
        <SidebarProvider>
            <AppSidebar>
                <div className="flex h-screen w-full p-2">
                    <ChatWindowLayout />
                </div>
            </AppSidebar>
            <Test />
        </SidebarProvider>
    )
}

export default ChatAppPage
