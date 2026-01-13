import { cn } from "@/lib/utils"
import { Card, CardContent } from "@/components/ui/card"
import { Label } from "../ui/label"
import { Input } from "../ui/input"
import { Button } from "../ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { signUpSchema, type SignUpRequest } from "@/types/auth"
import { useAuthStore } from "@/stores/useAuthStores"
import { useNavigate } from "react-router"
export function SignupForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  const { signUp } = useAuthStore();
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
  })

  const onSubmit = async (data: SignUpRequest) => {
    await signUp(data);
    navigate("/signin")
  }

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card className="overflow-hidden p-0 border-border">
        <CardContent className="grid p-0 md:grid-cols-2">
          <form className="p-6 md:p-8" onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-4">
              {/* header-logo */}
              <div className="flex flex-col items-center text-center gap-2">
                <a href="/" className="block w-fit mx-auto text-center">
                  <img src="/logo.svg" alt="logo" />
                </a>
                <h1 className="font-black text-2xl">Tạo tài khoản Moji</h1>
                <p className="text-balance text-muted-foreground">Chào mừng bạn! Hãy đăng ký để bắt đầu</p>
              </div>
              {/* họ và tên  */}
              <div className="flex flex-col gap-1">
                <Label htmlFor="displayName" className="block text-sm px-3" >Họ và tên</Label>
                <Input id="displayName" placeholder="Hãy nhập họ và tên của bạn" type="text" {...register("displayName")}></Input>
                {errors.displayName && <p className="text-red-500 text-sm px-3">{errors.displayName.message}</p>}
              </div>
              {/* username */}
              <div className="flex flex-col gap-1">
                <Label htmlFor="username" className="block text-sm px-3" >Tên đăng nhập</Label>
                <Input id="username" placeholder="Hãy nhập tên đăng nhập của bạn" type="text" {...register("username")}></Input>
                {errors.username && <p className="text-red-500 text-sm px-3">{errors.username.message}</p>}
              </div>
              {/* email  */}
              <div className="flex flex-col gap-1">
                <Label htmlFor="email" className="block text-sm px-3" >Email</Label>
                <Input id="email" placeholder="Hãy nhập email của bạn" type="email" {...register("email")}></Input>
                {errors.email && <p className="text-red-500 text-sm px-3">{errors.email.message}</p>}

              </div>
              {/* password */}
              <div className="flex flex-col gap-1">
                <Label htmlFor="password" className="block text-sm px-3" >Mật khẩu</Label>
                <Input id="password" placeholder="Hãy nhập mật khẩu của bạn" type="password" {...register("password")}></Input>
                {errors.password && <p className="text-red-500 text-sm px-3">{errors.password.message}</p>}

              </div>
              {/* nút đăng ký  */}
              <Button type="submit" className="w-80 mx-auto interceptor-loading" disabled={isSubmitting}>Tạo tài khoản</Button>
              <div className="text-sm text-center">Đã có tài khoản? {""} <a href="/signin" className="underline underline-offset-4"> Đăng nhập</a></div>
            </div>
          </form>
          <div className="bg-muted relative hidden md:block">
            <img
              src="/placeholderSignUp.png"
              alt="Image"
              className="absolute top-1/2 -translate-y-1/2 object-cover "
            />
          </div>
        </CardContent>
      </Card>
      <div className="text-sm text-balance px-6 text-center *:[a]:hover:text-primary text-muted-foreground *:[a]:underline *:[a]:underline-offset-4">
        Bằng cách tiếp tục, bạn đồng ý với <a href="#">Điều khoản dịch vụ</a>{" "}
        và <a href="#">Chính sách bảo mật của chúng tôi</a>.
      </div>
    </div>
  )
}
