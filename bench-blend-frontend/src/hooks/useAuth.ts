import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";
import axiosInstance from "@/lib/axios";
import { LoginRequest, ApiResponse, AuthResponse } from "@/types/auth";

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { setAuth, logout } = useAuthStore();
  const router = useRouter();

  const login = async (data: LoginRequest) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axiosInstance.post<ApiResponse<AuthResponse>>(
        "/auth/login",
        data
      );
      const { token, username, email } = response.data.data;
      setAuth(token, { username, email });
      router.push("/dashboard");
    } catch (err: any) {
      setError(
        err.response?.data?.message || "Login failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return { login, handleLogout, loading, error };
};