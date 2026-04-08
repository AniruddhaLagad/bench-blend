"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/authStore";

export default function DashboardPage() {
  const { isAuthenticated, user, logout, initAuth } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    initAuth();
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated]);

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navbar */}
      <nav className="bg-white shadow-sm px-8 py-4 flex justify-between items-center">
        <div>
          <h1 className="text-xl font-bold text-gray-800">BenchBlend</h1>
          <p className="text-xs text-gray-400">Modern College, Pune</p>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600">
            Welcome, <span className="font-medium">{user?.username}</span>
          </span>
          <button
            onClick={handleLogout}
            className="text-sm bg-red-50 hover:bg-red-100 text-red-600 px-4 py-2 rounded-lg transition-colors"
          >
            Logout
          </button>
        </div>
      </nav>

      {/* Content */}
      <main className="px-8 py-10">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-2xl font-semibold text-gray-800 mb-2">
            Dashboard
          </h2>
          <p className="text-gray-500 text-sm mb-8">
            Upload exam schedule and room skeleton to generate seating arrangements.
          </p>

          {/* Placeholder cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
              <h3 className="font-semibold text-gray-700 mb-1">Upload Files</h3>
              <p className="text-gray-400 text-sm">
                Upload exam schedule and room skeleton CSV/Excel files.
              </p>
            </div>
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
              <h3 className="font-semibold text-gray-700 mb-1">
                Generate Seating
              </h3>
              <p className="text-gray-400 text-sm">
                Run the seating algorithm and preview results.
              </p>
            </div>
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
              <h3 className="font-semibold text-gray-700 mb-1">Export</h3>
              <p className="text-gray-400 text-sm">
                Download filled seating arrangement as CSV or Excel.
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}