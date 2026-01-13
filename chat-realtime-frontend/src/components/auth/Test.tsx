import { Button } from '../ui/button'
import { toast } from 'sonner'
import authorizedAxiosInstance from '@/services/axios'

const Test = () => {

    const handleCallApi = async () => {
        try {
            // Gọi đến endpoint /test (Backend Java của bạn)
            const res = await authorizedAxiosInstance.get('/test');

            // Log ra kết quả hoặc hiển thị thông báo
            console.log("Dữ liệu nhận về:", res.data);
            toast.success(res.data?.message || "Gọi API thành công!");
        } catch (error: any) {
            // Lỗi đã được interceptor handle một phần (toast), 
            // nhưng bạn vẫn có thể xử lý riêng ở đây nếu cần
            console.error("Lỗi khi gọi API test:", error);
        }
    }

    return (
        <div className="flex items-center justify-center min-h-screen">
            <div className="p-6 border rounded-lg shadow-md bg-card">
                <h2 className="text-xl font-bold mb-4">Kiểm tra kết nối API</h2>
                <Button onClick={handleCallApi}>
                    Gửi Request tới /test
                </Button>
            </div>
        </div>
    )
}

export default Test