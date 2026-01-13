export interface ApiResponse<T> {
    statusCode: number; // Hoặc code tùy theo Backend trả về
    message?: string;
    data: T; // Chữ 'result' nên khớp hoàn toàn với field bên Java
}

// Đối với các API không trả về dữ liệu (như Register, Logout)
export type BaseResponse = ApiResponse<null>;