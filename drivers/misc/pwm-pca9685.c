/*
 * Driver for PCA9685 16-channel 12-bit PWM LED controller
 *
 * Copyright (C) 2013 Steffen Trumtrar <s.trumtrar@pengutronix.de>
 * Copyright (C) 2015 Clemens Gruber <clemens.gruber@pqgruber.com>
 *
 * based on the pwm-twl-led.c driver
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <linux/acpi.h>
#include <linux/i2c.h>
#include <linux/module.h>
#include <linux/platform_device.h>
#include <linux/pwm.h>
#include <linux/regmap.h>
#include <linux/slab.h>
#include <linux/delay.h>
#include <linux/err.h>
#include <linux/miscdevice.h>
#include <linux/fs.h>
#include <linux/gpio.h>
/*
 * Because the PCA9685 has only one prescaler per chip, changing the period of
 * one channel affects the period of all 16 PWM outputs!
 * However, the ratio between each configured duty cycle and the chip-wide
 * period remains constant, because the OFF time is set in proportion to the
 * counter range.
 */

#define PCA9685_MODE1		0x00
#define PCA9685_MODE2		0x01
#define PCA9685_SUBADDR1	0x02
#define PCA9685_SUBADDR2	0x03
#define PCA9685_SUBADDR3	0x04
#define PCA9685_ALLCALLADDR	0x05
#define PCA9685_LEDX_ON_L	0x06
#define PCA9685_LEDX_ON_H	0x07
#define PCA9685_LEDX_OFF_L	0x08
#define PCA9685_LEDX_OFF_H	0x09

#define PCA9685_ALL_LED_ON_L	0xFA
#define PCA9685_ALL_LED_ON_H	0xFB
#define PCA9685_ALL_LED_OFF_L	0xFC
#define PCA9685_ALL_LED_OFF_H	0xFD
#define PCA9685_PRESCALE	0xFE

#define PCA9685_PRESCALE_MIN	0x03	/* => max. frequency of 1526 Hz */
#define PCA9685_PRESCALE_MAX	0xFF	/* => min. frequency of 24 Hz */

#define PCA9685_COUNTER_RANGE	4096
#define PCA9685_DEFAULT_PERIOD	5000000	/* Default period_ns = 1/200 Hz */
#define PCA9685_OSC_CLOCK_MHZ	25	/* Internal oscillator with 25 MHz */

#define PCA9685_NUMREGS		0xFF
#define PCA9685_MAXCHAN		0x10

#define LED_FULL		(1 << 4)
#define MODE1_RESTART		(1 << 7)
#define MODE1_SLEEP		(1 << 4)
#define MODE2_INVRT		(1 << 4)
#define MODE2_OUTDRV		(1 << 2)

#define LED_N_ON_H(N)	(PCA9685_LEDX_ON_H + (4 * (N)))
#define LED_N_ON_L(N)	(PCA9685_LEDX_ON_L + (4 * (N)))
#define LED_N_OFF_H(N)	(PCA9685_LEDX_OFF_H + (4 * (N)))
#define LED_N_OFF_L(N)	(PCA9685_LEDX_OFF_L + (4 * (N)))

#define REG_BIT(n)			(1 << (n))
#define PCA9685_PWM_IOCTL_CODE  0x80
#define PCA9685_PWM_REQUEST   	_IOWR(PCA9685_PWM_IOCTL_CODE, 11, uint32_t)
#define PCA9685_PWM_FREE  	_IOWR(PCA9685_PWM_IOCTL_CODE, 12, uint32_t)
#define PCA9685_PWM_CONFIG   	_IOWR(PCA9685_PWM_IOCTL_CODE, 13, uint32_t)
#define PCA9685_PWM_UNCONFIG   	_IOWR(PCA9685_PWM_IOCTL_CODE, 14, uint32_t)
#define PCA9685_PWM_HZ   	_IOWR(PCA9685_PWM_IOCTL_CODE, 15, uint32_t)

#define GPIO_BASE		32
struct pca9685 {
	struct regmap *regmap;
	struct miscdevice misc;
	struct file_operations ops;
};
struct pca9685 *pca;

static void set_moto_power(int channel, int state)
{
	if (channel == 0)
	{
		gpio_request(2 + GPIO_BASE, "channel 0");
		gpio_set_value(2 + GPIO_BASE, state);
		gpio_free(2 + GPIO_BASE);
	}
	else if(channel == 1)
	{
		gpio_set_value(4 + 2* 32, state);
	}
}
static int pca9685_pwm_config(int channel, int on, int off)
{
	int on_h = (on & 0xf00) >> 8;
	int on_l = on &0xff;
	int off_h = (off & 0xf00)>>8;
	int off_l = off & 0xff;
	regmap_write(pca->regmap, LED_N_ON_H(channel), on_h);
	regmap_write(pca->regmap, LED_N_ON_L(channel), on_l);
	regmap_write(pca->regmap, LED_N_OFF_H(channel), off_h);
	regmap_write(pca->regmap, LED_N_OFF_L(channel), off_l);
	pr_info("%s config channel:%d, on:%d, off:%d\n", __func__, channel, on, off);
	return 0;
}

static int pca9685_pwm_unconfig(int channel)
{
	regmap_write(pca->regmap, LED_N_OFF_H(channel), 1<<4);
	regmap_write(pca->regmap, LED_N_OFF_L(channel), 0);
	pr_info("%s unconfig channel:%d\n", __func__, channel);
	return 0;
}

/*wake me up*/
static int pca9685_pwm_request(void)
{
	pr_info("%s \n", __func__);
	set_moto_power(0, 1);
	set_moto_power(1, 1);
	regmap_update_bits(pca->regmap, PCA9685_MODE2, MODE2_INVRT, MODE2_INVRT);
	return regmap_update_bits(pca->regmap, PCA9685_MODE1, MODE1_SLEEP, 0x0);
}
/*let me down*/
static int pca9685_pwm_free(void)
{
	pr_info("%s \n", __func__);
	regmap_write(pca->regmap, PCA9685_ALL_LED_OFF_L, 0);
	regmap_write(pca->regmap, PCA9685_ALL_LED_OFF_H, 0);
	set_moto_power(0, 0);
	set_moto_power(1, 0);

	regmap_update_bits(pca->regmap, PCA9685_MODE2, MODE2_INVRT, 0);
	return regmap_update_bits(pca->regmap, PCA9685_MODE1, MODE1_SLEEP, MODE1_SLEEP);
}
/*config prescale*/
static int pca9685_pwm_set_prescale(int hz)
{
	int val = 25*1000000/(4096 * hz) - 1;  
	regmap_update_bits(pca->regmap, PCA9685_MODE1, MODE1_SLEEP, MODE1_SLEEP);  
	regmap_write(pca->regmap, PCA9685_PRESCALE, val); 
	regmap_update_bits(pca->regmap, PCA9685_MODE1, MODE1_SLEEP, 0);  
	pr_info("%s set prescale:0x%xH\n", __func__, val);
	udelay(500);
	return regmap_update_bits(pca->regmap, PCA9685_MODE1, MODE1_RESTART, 0x1); 
}

static const struct regmap_config pca9685_regmap_i2c_config = {
	.reg_bits = 8,
	.val_bits = 8,
	.max_register = PCA9685_NUMREGS,
	.cache_type = REGCACHE_NONE,
};

static long pca9685_pwm_ioctl(struct file *file, unsigned int cmd, unsigned long arg)
{
	int ret;
	int channel, on, off;
	int prescale;
	switch(cmd)
	{
		case PCA9685_PWM_REQUEST:
			ret = pca9685_pwm_request();
			break;
		case PCA9685_PWM_FREE:
			ret = pca9685_pwm_free();
			break;
		case PCA9685_PWM_CONFIG:
			channel = (arg >> 24) & 0xff;
			on = (arg & 0x00fff000) >> 12;
			off = arg & 0xfff;
			pca9685_pwm_config(channel, on, off);
			break;
		case PCA9685_PWM_UNCONFIG:
			channel = arg;
			pca9685_pwm_unconfig(channel);
			break;
		case PCA9685_PWM_HZ:
			prescale = arg;
			pca9685_pwm_set_prescale(prescale);
			break;
	}
	return 0;
}



static int pca9685_pwm_probe(struct i2c_client *client,
		const struct i2c_device_id *id)
{
	int ret;
	int mode2;

	pr_info("%s\n", __func__);
	pca = devm_kzalloc(&client->dev, sizeof(*pca), GFP_KERNEL);
	if (!pca)
		return -ENOMEM;

	pca->regmap = devm_regmap_init_i2c(client, &pca9685_regmap_i2c_config);
	if (IS_ERR(pca->regmap)) {
		ret = PTR_ERR(pca->regmap);
		dev_err(&client->dev, "Failed to initialize register map: %d\n",
				ret);
		return ret;
	}

	pca->ops.unlocked_ioctl = pca9685_pwm_ioctl;
	i2c_set_clientdata(client, pca);

	ret = regmap_read(pca->regmap, PCA9685_MODE2, &mode2);
	if (ret < 0)
	{
		pr_err("%s i2c fail\n", __func__);
		return -ENODEV;
	}else
		pr_info("%s i2c read mode2:0x%x\n", __func__, mode2);
	/* clear all "full off" bits */
	regmap_write(pca->regmap, PCA9685_ALL_LED_OFF_L, 0);
	regmap_write(pca->regmap, PCA9685_ALL_LED_OFF_H, 0);

	set_moto_power(0, 0);
	set_moto_power(1, 0);


	pca->misc.name = "pca9685";
	pca->misc.minor = MISC_DYNAMIC_MINOR;
	pca->misc.fops = &pca->ops;
	ret = misc_register(&pca->misc);
	if (ret)
	{
		pr_err("%s misc register misc failed\n", __func__);
		//fix me
		//kfree
		return -ENODEV;
	}
	pr_info("%s succ\n", __func__);
	return 0;
}

static int pca9685_pwm_remove(struct i2c_client *client)
{

	regmap_update_bits(pca->regmap, PCA9685_MODE1, MODE1_SLEEP,
			MODE1_SLEEP);

	return 0;
}

static const struct i2c_device_id pca9685_id[] = {
	{ "pca9685", 0 },
	{ /* sentinel */ },
};
MODULE_DEVICE_TABLE(i2c, pca9685_id);

static struct i2c_driver pca9685_i2c_driver = {
	.driver = {
		.name = "pca9685",
		.owner = THIS_MODULE,
	},
	.probe = pca9685_pwm_probe,
	.remove = pca9685_pwm_remove,
	.id_table = pca9685_id,
};

module_i2c_driver(pca9685_i2c_driver);

MODULE_AUTHOR("OO");
MODULE_DESCRIPTION("PWM driver for PCA9685");
MODULE_LICENSE("GPL");
