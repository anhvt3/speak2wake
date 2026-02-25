# 🧠 Antigravity — Tổng hợp Skills & Knowledge Items

> **Máy:** Windows
> **Ngày tổng hợp:** 25/02/2026
> **Đường dẫn gốc:** `C:\Users\<username>\.gemini\antigravity\knowledge\`

Tài liệu này liệt kê toàn bộ **Skills** (kỹ năng chuyên biệt) và **Knowledge Items** (kiến thức tích luỹ) mà Antigravity đã học và lưu trữ. Mỗi mục là một "bộ não nhỏ" giúp Antigravity xử lý task chuyên biệt nhanh hơn.

---

## 📂 Tổng quan

| Loại | Số lượng |
|---|---|
| **Skills** (Agent chuyên biệt — có SKILL.md) | 3 |
| **Knowledge Items** (Kiến thức tích luỹ) | 12 |
| **Tổng cộng** | **15 mục** |

---

## ⚡ Skills (Agent chuyên biệt)

Skills là các "chế độ chuyên gia" — khi được kích hoạt, Antigravity sẽ hoạt động theo quy trình chuyên biệt nhất cho task đó.

### 1. 🧩 React Component Architect
- **Thư mục:** `react_component_agent/`
- **Kích hoạt khi:** "create a component", "build a UI element", "refactor a view"
- **Mô tả:** Agent chuyên xây dựng React component chất lượng cao. Sử dụng Vite + React 18 + Tailwind CSS + TypeScript. Có quy trình 4 bước: Analysis → Implementation → Template → Verification. Tự động kiểm tra accessibility (a11y), naming convention, và anti-patterns.
- **Tech:** React 18+, Vite, TypeScript Strict, Tailwind CSS, Lucide React, Zustand

### 2. 🗄️ MySQL Query Specialist
- **Thư mục:** `mysql_query_expert/`
- **Kích hoạt khi:** "query the database", "write SQL", "inspect tables"
- **Mô tả:** Agent chuyên viết và thực thi MySQL queries an toàn. Luôn kiểm tra schema trước khi query (MANDATORY). Mặc định READ-ONLY, format output dạng Markdown table. Tối ưu JOIN và tránh Cartesian Products.
- **Tech:** MySQL 8.0+, MCP tools (`mcp_mysql_read_query`, `mcp_mysql_desc_table`)

### 3. 🧪 Automated Tester Agent
- **Thư mục:** `automated_tester_agent/`
- **Kích hoạt khi:** "test this page", "verify the UI", "run tests", "check if this works"
- **Mô tả:** Agent QA kết hợp browser visual testing + unit tests + API testing. Quy trình 4 phase: Understand → Choose Strategy → Execute → Report. Tự động chụp screenshot trước/sau, tạo test report có bảng kết quả. Hỗ trợ Vitest, browser subagent, curl, và database verification.
- **Tech:** Browser Subagent, Vitest, curl, `mcp_mysql_read_query`

---

## 📚 Knowledge Items (Kiến thức tích luỹ)

Knowledge Items là kiến thức được tích luỹ qua các cuộc trò chuyện. Antigravity tự động tham khảo khi gặp task liên quan.

### 🤖 AI & Automation

#### 4. AI Agent Automation Framework
- **Thư mục:** `ai_agent_automation_framework/`
- **Artifacts:** 8 files
- **Mô tả:** Framework automation dùng LLM-based vision cho web interaction. v3.2+ có intelligent prompt system cho general web browsing và specialized quiz workflows. Features: centralized element marking, dynamic timeouts, stabilized selector list.

#### 5. AI-Powered 3D Character Animation & Video Workflows
- **Thư mục:** `ai_3d_animation_workflows/`
- **Artifacts:** 4 files
- **Mô tả:** Workflows tạo 3D animated character videos (mascots). Phân tích Tencent Hunyuan model series. Cloud-based walkthrough dùng Meshy.ai, Mixamo, Hedra/SyncLabs cho máy low-end (2GB VRAM).

#### 6. Hybrid AI Agent Orchestration (Gemini + Claude Code)
- **Thư mục:** `antigravity_hybrid_agent_orchestration/`
- **Artifacts:** 1 file
- **Mô tả:** Mô hình "Manager-Specialist" kết hợp Google Gemini (Antigravity, 2M token context) làm Project Manager và Claude Code (Sonnet 3.5) làm Senior Developer cho complex coding tasks.

---

### 🛠️ Tools & Integrations

#### 7. Google Stitch MCP Integration Guide
- **Thư mục:** `google_stitch_mcp/`
- **Artifacts:** 1 file
- **Mô tả:** Hướng dẫn kết nối AI tools với Google Stitch qua MCP. Hỗ trợ nhiều phương thức xác thực, cấu hình cho Antigravity/Cursor/VSCode/Claude Code. Tools cho project, screen, và design management.

#### 8. Figma Dev Mode MCP Integration
- **Thư mục:** `figma_mcp_integration/`
- **Artifacts:** 3 files
- **Mô tả:** Kết nối Figma Dev Mode với MCP clients. Local desktop server setup, remote cloud server alternatives, troubleshooting connection issues (ECONNREFUSED port 3845). Hỗ trợ authentication với community MCP servers.

#### 9. Lovable UI Automation Integration
- **Thư mục:** `lovable_ui_automation_integration/`
- **Artifacts:** 8 files
- **Mô tả:** Sync Lovable frontend với Automation Server. Agent API specs, Phase 2 Agent Goals management, connectivity strategies (ngrok), cloud deployment strategies (VPS, Render.com).

---

### 🖥️ Environment & DevOps

#### 10. Local Windows Environment Constraints
- **Thư mục:** `local_windows_environment/`
- **Artifacts:** 2 files (powershell_constraints, tool_availability)
- **Mô tả:** Constraints và workarounds cho Windows dev environment. PowerShell quirks, tool availability (missing Yarn/PIP), Python PATH, verified preference cho MySQL Shell.

#### 11. Antigravity Workspace & UI Management
- **Thư mục:** `antigravity_workspace_management/`
- **Artifacts:** 1 file
- **Mô tả:** Shortcuts và instructions cho quản lý Antigravity desktop environment. Mở thêm folders, multiple workspaces, hidden menus, keyboard shortcuts.

#### 12. Antigravity Browser Extension
- **Thư mục:** `antigravity_browser_extension/`
- **Artifacts:** 1 file
- **Mô tả:** Browser Extension kết nối AI agent với Chrome local. Tab control, navigation, viewport capture. Lưu ý: Vietnamese character input qua keyboard simulation bị lỗi — cần dùng JS-based workarounds.

#### 13. Software Packaging and Deployment Patterns
- **Thư mục:** `software_packaging_and_deployment/`
- **Artifacts:** 4 files
- **Mô tả:** So sánh Docker vs ZIP cho environment isolation. Hybrid patterns (Vercel/Cloud Run), Log Void mitigation, Multi-Environment Path & Binary Detection cho cross-platform subprocess stability.

---

### 📖 Guides & References

#### 14. Skill Creator Guide & Anthropic Skills Collection
- **Thư mục:** `skill_creator_guide/`
- **Artifacts:** 5 files
- **Mô tả:** Hướng dẫn tạo Skills mở rộng capabilities cho Claude/Antigravity. 6 bước: Understand → Plan → Initialize → Edit → Package → Iterate. Bao gồm 16 official Anthropic skills (frontend-design, webapp-testing, mcp-builder...).

#### 15. UI UX Pro Max Skill
- **Thư mục:** `ui_ux_pro_max_skill/`
- **Artifacts:** 13 files
- **Mô tả:** UI/UX design engine v2.0 với Python-powered reasoning system. 100+ rules, dataset 258+ items (67 styles, 96 palettes, 57 font pairings). Quantitative Design System hỗ trợ integration với aura.build và Anthropic frontend skills.

---

## 📊 Thống kê tổng hợp

| Phân loại | Số mục | Tổng artifacts |
|---|---|---|
| AI & Automation | 3 | ~13 files |
| Tools & Integrations | 3 | ~12 files |
| Environment & DevOps | 4 | ~8 files |
| Guides & References | 2 | ~18 files |
| **Skills (Agent chuyên biệt)** | **3** | **3 SKILL.md** |
| **Tổng** | **15** | **~54 artifact files** |

---

## 💡 Cách sử dụng

### Bước 1: Cài đặt phần mềm cần thiết

Mở terminal và yêu cầu Antigravity (hoặc tự cài):

```
cài Python (phiên bản 3.12+)
cài Node.js (phiên bản 20 LTS trở lên)
cài Git
cài Docker Desktop
cài MySQL Server 8.0+ (hoặc dùng Docker)
cài Google Chrome (cho browser extension / visual testing)
cài LaTeX — MiKTeX hoặc TeX Live (cho Manim vector animation)
cài Manim Community (pip install manim)
cài Firebase CLI (npm install -g firebase-tools)
cài Vercel CLI (npm install -g vercel)
cài Google Cloud CLI (gcloud)
```

> 💡 **Không cần cài hết** — chỉ cài theo skill/knowledge mà bạn cần dùng:
>
> | Bạn muốn dùng | Cần cài |
> |---|---|
> | React Component Architect | Node.js, Git |
> | MySQL Query Specialist | MySQL Server, Node.js |
> | Automated Tester Agent | Node.js, Chrome, Git |
> | AI Agent Automation | Python, Node.js, Chrome |
> | 3D Animation (Manim) | Python, LaTeX, Manim |
> | UI UX Pro Max | Python |
> | Deployment (Vercel/Firebase) | Node.js, Vercel CLI / Firebase CLI |
> | Google Stitch MCP | Node.js, Google Cloud CLI |
> | Figma MCP | Node.js |

### Bước 2: Cài Antigravity & Knowledge

1. **Copy toàn bộ thư mục `knowledge/`** vào `C:\Users\<username>\.gemini\antigravity\knowledge\` để có cùng bộ knowledge
2. **Chọn lọc** — chỉ copy các thư mục liên quan đến task cần làm
3. **Tạo thêm Skills mới** bằng cách tạo file `SKILL.md` theo format chuẩn trong thư mục knowledge

> ✅ **File này đã được sanitize** — không chứa credentials, API keys, hay thông tin nội bộ công ty nào. An toàn để chia sẻ.
